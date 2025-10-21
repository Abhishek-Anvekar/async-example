package com.healthcare.patient_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class AppointmentDtos {
    public record BookAppointmentRequest(
            @NotBlank String patientId,
            @NotBlank String doctorId,
            @NotNull LocalDateTime startTime,
            @NotNull Integer durationMinutes,
            @NotNull String slotId,
            String notes
    ) {}

    public record BookingResponse(String bookingId, String status, LocalDateTime startTime, Integer durationMinutes, String doctorId) {}
}
