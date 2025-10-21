package com.healthcare.doctor_service.service;

import com.healthcare.doctor_service.dto.AvailabilityDtos.CreateSlotsRequest;
import com.healthcare.doctor_service.entity.AvailabilitySlot;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.exception.BadRequestException;
import com.healthcare.doctor_service.exception.NotFoundException;
import com.healthcare.doctor_service.repository.AvailabilitySlotRepository;
import com.healthcare.doctor_service.repository.DoctorRepository;
import com.healthcare.doctor_service.service.messaging.DoctorEventsProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

@Service
public class AvailabilityService {

    private Logger logger = LoggerFactory.getLogger(AvailabilityService.class);
    private final AvailabilitySlotRepository repo;
    private final DoctorRepository doctorRepo;
    private final DoctorEventsProducer producer;

    public AvailabilityService(AvailabilitySlotRepository repo, DoctorRepository doctorRepo, DoctorEventsProducer producer) {
        this.repo = repo; this.doctorRepo = doctorRepo; this.producer = producer;
    }

    @Transactional
    public List<AvailabilitySlot> createSlots(String doctorId, CreateSlotsRequest req) {
        Doctor doctor = doctorRepo.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
        if (req.endTime().isBefore(req.startTime())) throw new BadRequestException("End time must be after start time");
        List<AvailabilitySlot> created = new ArrayList<>();
        LocalTime cursor = req.startTime();
        while (!cursor.plusMinutes(req.slotMinutes()).isAfter(req.endTime())) {
            AvailabilitySlot s = new AvailabilitySlot();
            s.setDoctor(doctor);
            s.setDate(req.date());
            s.setStartTime(cursor);
            s.setEndTime(cursor.plusMinutes(req.slotMinutes()));
            s.setMode(req.mode());
            repo.save(s);
            created.add(s);
            cursor = cursor.plusMinutes(req.slotMinutes());
        }
        producer.sendAvailabilityUpdated(doctorId);
        return created;
    }

    public List<AvailabilitySlot> listByDoctor(String doctorId){
        return repo.findByDoctorIdOrderByDateAscStartTimeAsc(doctorId);
    }

    /*
     * This method has been updated to prevent multiple users from booking the same slot concurrently
     * by leveraging Optimistic Locking.
     */
    @Transactional
    public void blockSlots(String doctorId, List<String> slotIds, boolean blocked){
        for (String id : slotIds) {
            boolean updated = false;
            int attempts = 0;

            while (!updated && attempts < 3) { // retry up to 3 times
                attempts++;
                try {
                    AvailabilitySlot slot = repo.findById(id)
                            .orElseThrow(() -> new NotFoundException("Slot not found: " + id));

                    if (!slot.getDoctor().getId().equals(doctorId))
                        throw new BadRequestException("Slot does not belong to doctor");

                    // Check if slot is already blocked before saving
                    if (slot.isBlocked() == blocked) { // no need to update if state is already desired
                        logger.info("Slot {} already in desired state (blocked={})", id, blocked);
                        updated = true;
                        break;
                    }

                    slot.setBlocked(blocked);
                    repo.save(slot); // triggers version check
                    updated = true;

                } catch (ObjectOptimisticLockingFailureException e) {
                    logger.warn("Optimistic lock conflict for slot {} (attempt {}/3)", id, attempts);
                    if (attempts == 3) {
                        logger.error("Failed to update slot {} after 3 retries", id);
                        throw new ConcurrentModificationException(
                                "Slot was modified by another user: " + id);
                    }
                    try {
                        Thread.sleep(100); // small delay before retry
                    } catch (InterruptedException ignored) {}
                }
            }
        }
        producer.sendAvailabilityUpdated(doctorId);
    }
}
