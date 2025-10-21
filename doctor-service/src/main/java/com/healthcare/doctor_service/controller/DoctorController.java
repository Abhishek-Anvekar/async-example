package com.healthcare.doctor_service.controller;

import com.healthcare.doctor_service.dto.DoctorDtos;
import com.healthcare.doctor_service.dto.PageResponse;
import com.healthcare.doctor_service.dto.PrescriptionDtos;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.service.AvailabilityService;
import com.healthcare.doctor_service.service.DoctorService;
import com.healthcare.doctor_service.service.PrescriptionService;
import com.healthcare.doctor_service.service.SearchService;
import com.healthcare.doctor_service.dto.*;
import com.healthcare.doctor_service.dto.DoctorDtos.*;
import com.healthcare.doctor_service.dto.AvailabilityDtos.*;
import com.healthcare.doctor_service.dto.PrescriptionDtos.*;
import com.healthcare.doctor_service.entity.*;
import com.healthcare.doctor_service.service.*;
import com.healthcare.doctor_service.util.Mappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
@Tag(name = "Doctor APIs", description = "Endpoints for managing doctor profiles, availability, reviews, prescriptions, and appointments")
public class DoctorController {

    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;
    private final PrescriptionService prescriptionService;
    private final SearchService searchService;

    public DoctorController(DoctorService doctorService, AvailabilityService availabilityService,
                            PrescriptionService prescriptionService, SearchService searchService) {
        this.doctorService = doctorService; this.availabilityService = availabilityService;
        this.prescriptionService = prescriptionService; this.searchService = searchService;
    }

    // ---- Registration / Verification / Activation ----

    @Operation(
            summary = "Register new doctor",
            description = "Creates a new doctor profile and returns doctor details after registration"
    )
    @ApiResponse(responseCode = "200", description = "Doctor registered successfully")
    @PostMapping("/register")
    public DoctorDtos.DoctorResponse register(@Valid @RequestBody RegisterDoctorRequest req){
        Doctor d = doctorService.register(req);
        return Mappers.toDoctorResponse(d);
    }

    @Operation(
            summary = "Verify doctor account",
            description = "Approves or rejects doctor verification"
    )
    @PutMapping("/{doctorId}/verify")
    public DoctorDtos.DoctorResponse verify(@PathVariable String doctorId, @RequestParam(defaultValue="true") boolean approve){
        return Mappers.toDoctorResponse(doctorService.verify(doctorId, approve));
    }

    @Operation(
            summary = "Activate/Deactivate doctor",
            description = "Enables or disables doctor account"
    )
    @PutMapping("/{doctorId}/activation")
    public DoctorDtos.DoctorResponse activation(@PathVariable String doctorId, @RequestParam boolean active){
        return Mappers.toDoctorResponse(doctorService.activate(doctorId, active));
    }

    // ---- Profile ----

    @Operation(summary = "Get doctor profile", description = "Fetches complete doctor details by ID")
    @GetMapping("/{doctorId}")
    public DoctorDtos.DoctorResponse get(@PathVariable String doctorId){
        return Mappers.toDoctorResponse(doctorService.get(doctorId));
    }

    //With Spring security we will check ROLE (we will manage role from API-Gateway so we are not using spring security in this service
//    @PutMapping("/{doctorId}/profile")
//    public DoctorDtos.DoctorResponse updateProfile(@PathVariable String doctorId,
//                                                   @Valid @RequestBody UpdateDoctorProfileRequest req,
//                                                   Authentication auth){
//        boolean isDoctor = auth!=null && auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_DOCTOR"));
//        String requesterId = auth!=null ? String.valueOf(auth.getPrincipal()) : null;
//        return Mappers.toDoctorResponse(doctorService.updateProfile(doctorId, req, requesterId, isDoctor));
//    }


    /**
     * NOTE: Role check (ROLE_DOCTOR) should be enforced at the API Gateway.
     * Add a gateway policy to allow only doctors to call this endpoint.
     */
    //For now we are not using spring security
    @Operation(summary = "Update doctor profile",
            description = "Allows doctor to update profile information" +
                    "Role check required: ROLE_DOCTOR (handled at API Gateway).")
    @PutMapping("/{doctorId}/profile")
    public DoctorDtos.DoctorResponse updateProfile(@PathVariable String doctorId,
                                                   @Valid @RequestBody UpdateDoctorProfileRequest req) {
        return Mappers.toDoctorResponse(doctorService.updateProfile(doctorId, req));
    }

    // ---- Search & Listing ----

    @Operation(summary = "Search doctors", description = "Search doctors by name, specialization, city, rating, etc.")
    @GetMapping
    public PageResponse<DoctorDtos.DoctorResponse> search(
            @RequestParam(required=false) String name,
            @RequestParam(required=false) String specialization,
            @RequestParam(required=false) String city,
            @RequestParam(required=false) Double minRating,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="12") int size,
            @RequestParam(defaultValue="fullName,asc") String sort
    ){
        var parts = sort.split(",");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts.length>1?parts[1]:"asc"), parts[0]));
        Page<Doctor> p = doctorService.search(name, specialization, city, minRating, pageable);
        var mapped = p.getContent().stream().map(Mappers::toDoctorResponse).toList();
        return new PageResponse<>(mapped, page, size, p.getTotalElements(), p.getTotalPages());
    }

    // ---- Availability ----

    @Operation(summary = "Create availability slots", description = "Doctor creates available time slots for appointments")
    @PostMapping("/{doctorId}/availability/slots")
    public List<SlotResponse> createSlots(@PathVariable String doctorId, @Valid @RequestBody CreateSlotsRequest req){
        return availabilityService.createSlots(doctorId, req).stream().map(Mappers::toSlotResponse).toList();
    }

    @Operation(summary = "List availability slots", description = "Fetch all availability slots for doctor")
    @GetMapping("/{doctorId}/availability/slots")
    public List<SlotResponse> listSlots(@PathVariable String doctorId){
        return availabilityService.listByDoctor(doctorId).stream().map(Mappers::toSlotResponse).toList();
    }

    @Operation(
            summary = "Block / Unblock availability slots",
            description = "Blocks or unblocks a list of availability slot IDs for the given doctor. " +
                    "Role check required: ROLE_DOCTOR (handled at API Gateway)."
    )
    @ApiResponse(responseCode = "200", description = "Number of slots updated")
    @PutMapping("/{doctorId}/availability/slots/block")
    public ResponseEntity<?> block(
            @Parameter(description = "Doctor ID", required = true)@PathVariable String doctorId,
                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                           description = "Payload containing slot IDs to toggle and the blocked flag",
                                           required = true,
                                           content = @Content(schema = @Schema(implementation = ToggleBlockRequest.class))
                                   )@RequestBody ToggleBlockRequest req){
        availabilityService.blockSlots(doctorId, req.slotIds(), req.blocked());
        return ResponseEntity.ok(Map.of("updated", req.slotIds().size(),"blocked", req.blocked()));
    }

    // ---- Appointments (via appointment-service) ----

    @Operation(summary = "Upcoming appointments", description = "List of upcoming appointments for doctor")
    @GetMapping("/{doctorId}/appointments/upcoming")
    public List<Map<String,Object>> upcoming(@PathVariable String doctorId){
        return searchService.upcomingAppointmentsForDoctor(doctorId);
    }

    @Operation(summary = "Past appointments", description = "Fetch doctor’s appointment history")
    @GetMapping("/{doctorId}/appointments/history")
    public List<Map<String,Object>> history(@PathVariable String doctorId){
        return searchService.pastAppointmentsForDoctor(doctorId);
    }

    // ---- Reviews (via review-service) ----

    @Operation(summary = "Get doctor reviews", description = "Fetch reviews for a doctor")
    @GetMapping("/{doctorId}/reviews")
    public List<Map<String,Object>> reviews(@PathVariable String doctorId){
        return searchService.reviewsForDoctor(doctorId);
    }

    @Operation(summary = "Refresh rating", description = "Recalculate doctor’s rating from reviews")
    @PostMapping("/{doctorId}/reviews/refresh-rating")
    public Map<String,Object> refreshRating(@PathVariable String doctorId){
        double rating = searchService.refreshRatingFromReviews(doctorId);
        return Map.of("doctorId", doctorId, "rating", rating);
    }

    // ---- Prescriptions ----

    //With Spring security we will check ROLE (we will manage role from API-Gateway so we are not using spring security in this service
//    @PostMapping("/{doctorId}/prescriptions")
//    public PrescriptionDtos.PrescriptionResponse createRx(@PathVariable String doctorId, @Valid @RequestBody CreatePrescriptionRequest req, Authentication auth){
//        // Optional: ensure doctorId == auth.principal for ROLE_DOCTOR
//        var isDoctor = auth!=null && auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_DOCTOR"));
//        if (isDoctor && !String.valueOf(auth.getPrincipal()).equals(doctorId))
//            throw new ForbiddenException("Cannot create prescription for another doctor");
//        var p = prescriptionService.create(doctorId, req);
//        return Mappers.toPrescriptionResponse(p);
//    }

    /**
     * NOTE: Role check (ROLE_DOCTOR) should be enforced at the API Gateway.
     * Add a gateway policy to allow only doctors to call this endpoint.
     */
    //For now we are not using spring security
    @Operation(summary = "Create prescription",
            description = "Doctor creates a new prescription for patient" +
                    "Role check required: ROLE_DOCTOR (handled at API Gateway).")
    @PostMapping("/{doctorId}/prescriptions")
    public PrescriptionDtos.PrescriptionResponse createRx(
            @PathVariable String doctorId,
            @Valid @RequestBody CreatePrescriptionRequest req) {

        var prescription = prescriptionService.create(doctorId, req);
        return Mappers.toPrescriptionResponse(prescription);
    }

    @Operation(summary = "List prescriptions", description = "Fetch all prescriptions created by a doctor")
    @GetMapping("/{doctorId}/prescriptions")
    public List<PrescriptionDtos.PrescriptionResponse> listRx(@PathVariable String doctorId){
        return prescriptionService.byDoctor(doctorId).stream().map(Mappers::toPrescriptionResponse).toList();
    }
}
