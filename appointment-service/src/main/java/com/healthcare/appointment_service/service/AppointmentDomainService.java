package com.healthcare.appointment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.appointment_service.dto.AppointmentDtos.*;
import com.healthcare.appointment_service.entity.Appointment;
import com.healthcare.appointment_service.exception.BadRequestException;
import com.healthcare.appointment_service.exception.NotFoundException;
import com.healthcare.appointment_service.repository.AppointmentRepository;
import com.healthcare.appointment_service.util.Ids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class AppointmentDomainService {

    private final AppointmentRepository repo;
    private final ExternalServiceClient external;
    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper = new ObjectMapper();

    private final String topicConfirm;
    private final String topicReject;
    private final String topicCancel;
    private final String topicNotify;

    public AppointmentDomainService(AppointmentRepository repo,
                                    ExternalServiceClient external,
                                    KafkaTemplate<String, String> kafka,
                                    @Value("${kafka.topics.confirmed}") String topicConfirm,
                                    @Value("${kafka.topics.rejected}") String topicReject,
                                    @Value("${kafka.topics.cancelled}") String topicCancel,
                                    @Value("${kafka.topics.notify}") String topicNotify) {
        this.repo = repo;
        this.external = external;
        this.kafka = kafka;
        this.topicConfirm = topicConfirm;
        this.topicReject = topicReject;
        this.topicCancel = topicCancel;
        this.topicNotify = topicNotify;
    }

    @Transactional
    public Appointment book(BookAppointmentRequest req){
        if (req.durationMinutes() <= 0) throw new BadRequestException("durationMinutes must be > 0");

        // fetch doctor & patient in parallel (CompletableFuture)
        CompletableFuture<Map<String,Object>> fDoctor = external.getDoctorProfileAsync(req.doctorId());
        CompletableFuture<Map<String,Object>> fPatient = external.getPatientAsync(req.patientId());
        CompletableFuture.allOf(fDoctor, fPatient).join();
        Map<String,Object> doctor = fDoctor.join();
        Map<String,Object> patient = fPatient.join();
        if (doctor == null || doctor.isEmpty()) throw new BadRequestException("Invalid doctorId");
        if (patient == null || patient.isEmpty()) throw new BadRequestException("Invalid patientId");

        // validate and (optionally) lock the slot in doctor-service
        boolean ok = external.validateAndLockSlot(req.doctorId(), req.slotId(), req.startTime(), req.durationMinutes());
        if (!ok) throw new BadRequestException("Requested slot is not available");

        // Prevent double booking at same doctor+time
        repo.findFirstByDoctorIdAndStartTimeAndStatusIn(req.doctorId(), req.startTime(),
                        List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED))
                .ifPresent(a -> { throw new BadRequestException("Slot already booked"); });

        Appointment a = Appointment.builder()
                .id(Ids.newId())
                .doctorId(req.doctorId())
                .patientId(req.patientId())
                .patientPhone(String.valueOf(patient.get("phone")))
                .startTime(req.startTime())
                .durationMinutes(req.durationMinutes())
                .status(AppointmentStatus.PENDING)
                .paymentStatus(PaymentStatus.NA)
                .slotId(req.slotId())
                .notes(req.notes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Appointment saved = repo.save(a);

        // Notify (OTP/confirmation workflow handled by messaging-service)
        sendNotify("APPOINTMENT_CREATED", saved); // we are not using kafka here. Its just async-example so all kafka publishing code need to comment in order to run application

        return saved;
    }

    @Transactional
    public Appointment confirm(String id) throws JsonProcessingException {
        Appointment a = get(id);
        if (a.getStatus() != AppointmentStatus.PENDING) throw new BadRequestException("Only PENDING can be confirmed");
        a.setStatus(AppointmentStatus.CONFIRMED);
        a.setUpdatedAt(LocalDateTime.now());
        repo.save(a);

        // Convert LocalDateTime to String because Jackson cannot directly serialize LocalDateTime in Map payload
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String startTime = a.getStartTime().format(formatter);

        CompletableFuture<Map<String,Object>> fDoctor = external.getDoctorProfileAsync(a.getDoctorId());
        Map<String,Object> doctor = fDoctor.join();
//        System.out.println("FROM AppointmentDomainService~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+doctor);

        Map<String, Object> payload = Map.of("appointmentId", a.getId(), "doctorId", a.getDoctorId(), "patientId", a.getPatientId(), "patientPhone", a.getPatientPhone(), "startTime", startTime, "fullName", doctor.get("fullName"));
        //System.out.println("~~~~~~~~~~~~~~~~~~~Publishing to Kafka: " + new ObjectMapper().writeValueAsString(payload));
        publish(topicConfirm, payload);
        sendNotify("APPOINTMENT_CONFIRMED", a);
        return a;
    }

    @Transactional
    public Appointment reject(String id, String reason){
        Appointment a = get(id);
        if (a.getStatus() != AppointmentStatus.PENDING) throw new BadRequestException("Only PENDING can be rejected");
        a.setStatus(AppointmentStatus.REJECTED);
        a.setUpdatedAt(LocalDateTime.now());
        repo.save(a);

        publish(topicReject, Map.of("appointmentId", a.getId(), "reason", reason, "patientPhone",a.getPatientPhone()));
        // unlock previously locked slot if we have slotId
        external.unlockSlotIfNeeded(a.getDoctorId(), a.getSlotId());
        sendNotify("APPOINTMENT_REJECTED", a);
        return a;
    }

    @Transactional
    public Appointment cancel(String id, boolean byDoctor){
        Appointment a = get(id);
        if (a.getStatus() == AppointmentStatus.CANCELLED) return a;
        if (a.getStatus() == AppointmentStatus.COMPLETED) throw new BadRequestException("Completed cannot be cancelled");
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setUpdatedAt(LocalDateTime.now());
        repo.save(a);

        publish(topicCancel, Map.of("appointmentId", a.getId(), "byDoctor", byDoctor, "patientPhone",a.getPatientPhone()));
        external.unlockSlotIfNeeded(a.getDoctorId(), a.getSlotId());
        sendNotify("APPOINTMENT_CANCELLED", a);
        return a;
    }

    @Transactional
    public Appointment reschedule(String id, RescheduleRequest req){
        Appointment a = get(id);
        if (!(a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.CONFIRMED))
            throw new BadRequestException("Only PENDING/CONFIRMED can be rescheduled");

        // Validate new slot
        boolean ok = external.validateAndLockSlot(a.getDoctorId(), req.newSlotId(), req.newStartTime(), req.newDurationMinutes());
        if (!ok) throw new BadRequestException("New slot not available");

        // Unlock old slot if we had one
        external.unlockSlotIfNeeded(a.getDoctorId(), a.getSlotId());

        a.setStartTime(req.newStartTime());
        a.setDurationMinutes(req.newDurationMinutes());
        a.setSlotId(req.newSlotId());
        a.setUpdatedAt(LocalDateTime.now());
        repo.save(a);

        sendNotify("APPOINTMENT_RESCHEDULED", a);
        return a;
    }

    @Transactional
    public Appointment complete(String id){
        Appointment a = get(id);
        if (a.getStatus() != AppointmentStatus.CONFIRMED) throw new BadRequestException("Only CONFIRMED can be completed");
        a.setStatus(AppointmentStatus.COMPLETED);
        a.setUpdatedAt(LocalDateTime.now());
        repo.save(a);

        sendNotify("APPOINTMENT_COMPLETED", a);
        return a;
    }

    public Appointment get(String id){
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Appointment not found"));
    }

    public List<Appointment> upcomingForDoctor(String doctorId){
        return repo.findByDoctorIdAndStatusInOrderByStartTimeAsc(doctorId,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));
    }

    public List<Appointment> pastForDoctor(String doctorId){
        return repo.findByDoctorIdAndStatusInOrderByStartTimeDesc(doctorId,
                List.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED));
    }

    public List<Appointment> pastForPatient(Long patientId){
        return repo.findByPatientIdAndStatusInOrderByStartTimeDesc(patientId,
                List.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED));
    }

    public List<Appointment> forPatient(Long patientId){
        return repo.findByPatientIdOrderByStartTimeDesc(patientId);
    }

    private void publish(String topic, Map<String,Object> payload){
        try { kafka.send(topic, mapper.writeValueAsString(payload)); }
        catch (Exception ignored) { }
    }

    private void sendNotify(String type, Appointment a){
        publish(topicNotify, Map.of(
                "type", type,
                "appointmentId", a.getId(),
                "doctorId", a.getDoctorId(),
                "patientId", a.getPatientId(),
                "startTime", a.getStartTime().toString()
        ));
    }

    public List<Appointment> findBetween(LocalDateTime from, LocalDateTime to) {
        return repo.findByStartTimeBetween(from, to);
    }
}
