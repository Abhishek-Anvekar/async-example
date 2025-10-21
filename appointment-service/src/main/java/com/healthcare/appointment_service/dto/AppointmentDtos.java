package com.healthcare.appointment_service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class AppointmentDtos {

    public enum AppointmentStatus { PENDING, CONFIRMED, REJECTED, CANCELLED, COMPLETED }
    public enum PaymentStatus { PENDING, PAID, REFUNDED, NA }

    public record BookAppointmentRequest(
            @NotBlank String doctorId,
            @NotNull Long patientId,
            @NotNull LocalDateTime startTime,
            @NotNull @Positive Integer durationMinutes,
            String slotId, // optional if doctor-service supplies a slot id
            String notes
    ) { }

    public record RescheduleRequest(
            @NotNull LocalDateTime newStartTime,
            @NotNull @Positive Integer newDurationMinutes,
            String newSlotId
    ) { }

    public record AppointmentResponse(
            String id,
            String doctorId,
            Long patientId,
            LocalDateTime startTime,
            Integer durationMinutes,
            AppointmentStatus status,
            PaymentStatus paymentStatus,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) { }

    public record BulkResponse(List<AppointmentResponse> items) { }
}
