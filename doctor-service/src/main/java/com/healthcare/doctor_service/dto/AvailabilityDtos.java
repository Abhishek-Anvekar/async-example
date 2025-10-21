package com.healthcare.doctor_service.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AvailabilityDtos {
    public record CreateSlotsRequest(
            @NotNull LocalDate date,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @Positive int slotMinutes,
            @NotBlank String mode // ONLINE | CLINIC | HOME
    ) {}

    public record SlotResponse(
            String id, LocalDate date, LocalTime startTime, LocalTime endTime,
            String mode, boolean blocked
    ) {}

    public record ToggleBlockRequest(@NotNull List<String> slotIds, boolean blocked) {}
}
