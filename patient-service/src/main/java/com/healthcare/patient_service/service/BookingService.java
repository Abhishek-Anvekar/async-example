package com.healthcare.patient_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.patient_service.dto.AppointmentDtos.*;
import com.healthcare.patient_service.entity.Booking;
import com.healthcare.patient_service.exception.BadRequestException;
import com.healthcare.patient_service.exception.NotFoundException;
import com.healthcare.patient_service.repository.BookingRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {

    private final BookingRepository repo;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExternalServiceClient externalClient;

    public BookingService(BookingRepository repo, KafkaTemplate<String,String> kafka, ExternalServiceClient externalClient){
        this.repo = repo; this.kafka = kafka;
        this.externalClient = externalClient;
    }

    @Transactional
    public Booking book(BookAppointmentRequest req){
        // Basic validations
        if (req.durationMinutes() <= 0) throw new BadRequestException("durationMinutes must be > 0");
        Booking b = Booking.builder()
                .bookingId(UUID.randomUUID().toString())
                .slotId(req.slotId())
                .patientId(Long.parseLong(req.patientId()))
                .doctorId(req.doctorId())
                .startTime(req.startTime())
                .durationMinutes(req.durationMinutes())
                .status("PENDING")
                .notes(req.notes())
                .createdAt(LocalDateTime.now())
                .build();
        b = repo.save(b);

        // Check if slotId exists. validation to pass valid slotId to the Appointment service
        List<Map<String, Object>> slots = externalClient.fetchDoctorAvailability(req.doctorId());
        Map<String, Object> slot = slots.stream()
                .filter(s -> req.slotId().equals(String.valueOf(s.get("id")))) // we are created 'slotId' field as 'id' in doctor-service. you can check in available slot response
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Invalid slotId: " + req.slotId()));

        // Check if slot is already blocked
        boolean blocked = Boolean.TRUE.equals(slot.get("blocked"));
        if (blocked) {
            throw new BadRequestException("Slot already booked");
        }

        // Publish kafka event so appointment-service picks up and creates actual appointment
        Map<String,Object> evt = new HashMap<>();
        evt.put("bookingId", b.getBookingId());
        evt.put("slotId", b.getSlotId());
        evt.put("patientId", b.getPatientId());
        evt.put("doctorId", b.getDoctorId());
        evt.put("startTime", b.getStartTime().toString());
        evt.put("durationMinutes", b.getDurationMinutes());
        evt.put("notes", b.getNotes());

        String payload;
        try {
            payload = objectMapper.writeValueAsString(evt);
        } catch (JsonProcessingException e){
            throw new RuntimeException(e);
        }
        kafka.send("appointment-book-request", payload);

        // Also publish event for notifications (messaging service)
        kafka.send("user-notify-request", "BOOKING_CREATED:"+b.getBookingId());

        return b;
    }

    public Booking getById(Long id){ return repo.findById(id).orElseThrow(() -> new NotFoundException("Booking not found")); }

    public List<Booking> getForPatient(Long patientId){ return repo.findByPatientIdOrderByStartTimeDesc(patientId); }

    @Transactional
    public Booking cancel(String bookingId){
        Booking b = repo.findByBookingId(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
        if ("CANCELLED".equalsIgnoreCase(b.getStatus())) throw new BadRequestException("Already cancelled");
        b.setStatus("CANCELLED");
        repo.save(b);
        kafka.send("appointment-cancel-request", bookingId);
        kafka.send("user-notify-request", "BOOKING_CANCELLED:"+bookingId);
        return b;
    }
}