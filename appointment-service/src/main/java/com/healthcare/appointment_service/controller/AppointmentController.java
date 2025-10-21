package com.healthcare.appointment_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.healthcare.appointment_service.dto.AppointmentDtos.*;
import com.healthcare.appointment_service.entity.Appointment;
import com.healthcare.appointment_service.service.AppointmentDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointment APIs", description = "Book, manage, and query appointments")
public class AppointmentController {

    private final AppointmentDomainService service;

    public AppointmentController(AppointmentDomainService service) {
        this.service = service;
    }

    // Role: ROLE_PATIENT (enforced at API Gateway)
    @Operation(summary = "Book appointment", description = "Patient books an appointment with a doctor. Validates and locks doctor slot.")
    @PostMapping
    public ResponseEntity<AppointmentResponse> book(@Valid @RequestBody BookAppointmentRequest req){
        Appointment a = service.book(req);
        return ResponseEntity.ok(toResp(a));
    }

    // Role: ROLE_DOCTOR or ADMIN (Gateway)
    @Operation(summary = "Confirm appointment", description = "Doctor/admin confirms a pending appointment")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable String id) throws JsonProcessingException {
        return ResponseEntity.ok(toResp(service.confirm(id)));
    }

    // Role: ROLE_DOCTOR or ADMIN (Gateway)
    @Operation(summary = "Reject appointment", description = "Doctor/admin rejects a pending appointment")
    @PutMapping("/{id}/reject")
    public ResponseEntity<AppointmentResponse> reject(@PathVariable String id, @RequestParam(required=false) String reason){
        return ResponseEntity.ok(toResp(service.reject(id, reason)));
    }

    // Role: ROLE_PATIENT or ROLE_DOCTOR (Gateway)
    @Operation(summary = "Cancel appointment", description = "Cancel an appointment (by doctor or patient).")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(@PathVariable String id,
                                                      @RequestParam(defaultValue = "false") boolean byDoctor){
        return ResponseEntity.ok(toResp(service.cancel(id, byDoctor)));
    }

    // Role: ROLE_PATIENT or ROLE_DOCTOR (Gateway)
    @Operation(summary = "Reschedule appointment", description = "Reschedule appointment to a new time/slot")
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> reschedule(@PathVariable String id, @Valid @RequestBody RescheduleRequest req){
        return ResponseEntity.ok(toResp(service.reschedule(id, req)));
    }

    // Role: ROLE_DOCTOR (Gateway)
    @Operation(summary = "Complete appointment", description = "Mark a confirmed appointment as completed")
    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(@PathVariable String id){
        return ResponseEntity.ok(toResp(service.complete(id)));
    }

    // Role: ROLE_DOCTOR (Gateway)
    @Operation(summary = "Upcoming for doctor", description = "List pending/confirmed appointments for doctor")
    @GetMapping("/doctor/{doctorId}/upcoming")
    public ResponseEntity<BulkResponse> upcomingForDoctor(@PathVariable String doctorId){
        List<Appointment> list = service.upcomingForDoctor(doctorId);
        return ResponseEntity.ok(new BulkResponse(list.stream().map(this::toResp).toList()));
    }

    @Operation(summary = "Past appointments for doctor", description = "List completed/cancelled appointments for doctor")
    @GetMapping("/doctor/{doctorId}/past")
    public ResponseEntity<BulkResponse> pastForDoctor(@PathVariable String doctorId){
        List<Appointment> list = service.pastForDoctor(doctorId);
        return ResponseEntity.ok(new BulkResponse(list.stream().map(this::toResp).toList()));
    }

    @Operation(summary = "Past appointments for patient", description = "List completed/cancelled appointments for patient")
    @GetMapping("/patient/{patientId}/past")
    public ResponseEntity<BulkResponse> pastForPatient(@PathVariable Long patientId){
        List<Appointment> list = service.pastForPatient(patientId);
        return ResponseEntity.ok(new BulkResponse(list.stream().map(this::toResp).toList()));
    }

    // Role: ROLE_PATIENT (Gateway)
    @Operation(summary = "Appointments for patient", description = "List all appointments for a patient")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<BulkResponse> forPatient(@PathVariable Long patientId){
        List<Appointment> list = service.forPatient(patientId);
        return ResponseEntity.ok(new BulkResponse(list.stream().map(this::toResp).toList()));
    }

    private AppointmentResponse toResp(Appointment a){
        return new AppointmentResponse(
                a.getId(), a.getDoctorId(), a.getPatientId(),
                a.getStartTime(), a.getDurationMinutes(), a.getStatus(), a.getPaymentStatus(),
                a.getNotes(), a.getCreatedAt(), a.getUpdatedAt()
        );
    }

    @Operation(summary = "Search appointments", description = "Find appointments within a given time range")
    @GetMapping("/search")
    public ResponseEntity<BulkResponse> search(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<Appointment> list = service.findBetween(from, to);
        return ResponseEntity.ok(new BulkResponse(list.stream().map(this::toResp).toList()));
    }
}
