package com.healthcare.doctor_service.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.List;

public class PrescriptionDtos {
    public record CreatePrescriptionRequest(
            @NotBlank String appointmentId,
            @NotBlank String patientId,
            @NotEmpty List<String> medications,   // e.g. "Amoxicillin 500mg - 1-0-1 (5 days)"
            List<String> labTests,
            String advice
    ) {}

    public record PrescriptionResponse(
            String id, String appointmentId, String patientId, String doctorId,
            List<String> medications, List<String> labTests, String advice,
            OffsetDateTime issuedAt
    ) {}
}
