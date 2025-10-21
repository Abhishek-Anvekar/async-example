package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.dto.AppointmentDtos;
import com.healthcare.patient_service.dto.AppointmentDtos.*;
import com.healthcare.patient_service.dto.PatientDtos;
import com.healthcare.patient_service.dto.PatientDtos.*;
import com.healthcare.patient_service.dto.PaymentDtos.PaymentIntentResponse;
import com.healthcare.patient_service.entity.Booking;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.service.BookingService;
import com.healthcare.patient_service.service.ExternalServiceClient;
import com.healthcare.patient_service.service.PatientService;
import com.healthcare.patient_service.service.PaymentService;
import com.healthcare.patient_service.service.*;
import com.healthcare.patient_service.util.Mappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * NOTE: Role checks (ROLE_PATIENT) should be enforced at API Gateway.
 * Add comment lines on methods below indicate where the gateway should validate roles.
 */
@RestController
@RequestMapping("/patients")
@Tag(name = "Patient APIs", description = "Manage patient profile, bookings, payments and reviews")
public class PatientController {

    private final PatientService patientService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final ExternalServiceClient externalClient;

    public PatientController(PatientService patientService, BookingService bookingService,
                             PaymentService paymentService, ExternalServiceClient externalClient){
        this.patientService = patientService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.externalClient = externalClient;
    }

    // ---------------- Patient Profile ----------------

    @Operation(summary = "Create patient profile", description = "Register a new patient")
    @PostMapping
    public ResponseEntity<PatientDtos.PatientResponse> create(@Valid @RequestBody CreatePatientRequest req){
        Patient p = patientService.create(req);
        return ResponseEntity.ok(Mappers.toResponse(p));
    }

    @Operation(summary = "Get patient", description = "Get patient details by id")
    @GetMapping("/{id}")
    public ResponseEntity<PatientDtos.PatientResponse> get(@PathVariable Long id){
        Patient p = patientService.get(id);
        return ResponseEntity.ok(Mappers.toResponse(p));
    }

    @Operation(summary = "Update patient", description = "Update patient profile")
    // Role-check at API gateway: ROLE_PATIENT (or admin) should be allowed to update own profile
    @PutMapping("/{id}")
    public ResponseEntity<PatientDtos.PatientResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePatientRequest req){
        Patient p = patientService.update(id, req);
        return ResponseEntity.ok(Mappers.toResponse(p));
    }

    @Operation(summary = "List patients", description = "List all patients (admin usage)")
    // Role-check: ADMIN at gateway
    @GetMapping
    public ResponseEntity<List<PatientDtos.PatientResponse>> list(){
        return ResponseEntity.ok(patientService.listAll().stream().map(Mappers::toResponse).collect(Collectors.toList()));
    }

    // ---------------- Bookings (Patient-facing) ----------------

    @Operation(summary = "Book appointment", description = "Book an appointment with a doctor")
    // Role-check: ROLE_PATIENT at gateway
    @PostMapping("/book")
    public ResponseEntity<AppointmentDtos.BookingResponse> book(@Valid @RequestBody BookAppointmentRequest req){
        Booking b = bookingService.book(req);
        return ResponseEntity.ok(new BookingResponse(b.getBookingId(), b.getStatus(), b.getStartTime(), b.getDurationMinutes(), b.getDoctorId()));
    }

    @Operation(summary = "Cancel booking", description = "Cancel a booking")
    // Role-check: ROLE_PATIENT at gateway
    @PostMapping("/book/{bookingId}/cancel")
    public ResponseEntity<Map<String,Object>> cancel(@PathVariable String bookingId){
        Booking b = bookingService.cancel(bookingId);
        return ResponseEntity.ok(Map.of("bookingId", b.getBookingId(), "status", b.getStatus()));
    }

    @Operation(summary = "Patient bookings", description = "List bookings for a patient")
    // Role-check: ROLE_PATIENT at gateway
    @GetMapping("/{patientId}/bookings")
    public ResponseEntity<List<AppointmentDtos.BookingResponse>> myBookings(@PathVariable Long patientId){
        var list = bookingService.getForPatient(patientId).stream().map(b ->
                new BookingResponse(b.getBookingId(), b.getStatus(), b.getStartTime(), b.getDurationMinutes(), b.getDoctorId())
        ).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{patientId}/appointments/history")
    public ResponseEntity<List<Map<String,Object>>> getPastAppointments(@PathVariable Long patientId){
        List<Map<String,Object>> list = externalClient.pastAppointmentsForPatient(patientId);
        return ResponseEntity.ok(list);
    }

    // ---------------- Payments ----------------

    @Operation(summary = "Create payment intent", description = "Create payment intent for appointment")
    // Role-check: ROLE_PATIENT at gateway
    @PostMapping("/payments/intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(@RequestParam long amount, @RequestParam(defaultValue = "INR") String currency){
        return ResponseEntity.ok(paymentService.createIntent(amount, currency));
    }

    // ---------------- Doctor discovery / details (consumed from doctor-service) ----------------

    @Operation(summary = "Get doctor profile", description = "Fetch doctor profile from doctor-service")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Map<String,Object>> getDoctorProfile(@PathVariable String doctorId){
        return ResponseEntity.ok(externalClient.fetchDoctorProfile(doctorId));
    }

    @Operation(summary = "Get doctor availability", description = "Fetch doctor's availability from doctor-service")
    @GetMapping("/doctor/{doctorId}/availability")
    public ResponseEntity<List<Map<String,Object>>> getDoctorAvailability(@PathVariable String doctorId){
        return ResponseEntity.ok(externalClient.fetchDoctorAvailability(doctorId));
    }

    @Operation(summary = "Get doctor reviews", description = "Fetch doctor's reviews from doctor-service")
    @GetMapping("/doctor/{doctorId}/reviews")
    public ResponseEntity<List<Map<String,Object>>> getDoctorReviews(@PathVariable String doctorId){
        return ResponseEntity.ok(externalClient.fetchDoctorReviews(doctorId));
    }
}