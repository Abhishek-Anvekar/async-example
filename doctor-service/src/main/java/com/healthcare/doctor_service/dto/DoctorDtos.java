package com.healthcare.doctor_service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class DoctorDtos {
    public record RegisterDoctorRequest(
            @NotBlank String fullName,
            @NotBlank String specialization,
            @NotBlank String city,
            @PositiveOrZero BigDecimal consultationFee,
            @NotBlank String phone,
            @Email String email,
            @NotBlank String licenseNumber,
            List<String> clinicAddresses
    ) {}

    public record UpdateDoctorProfileRequest(
            String fullName, String specialization, String city, double rating,
            BigDecimal consultationFee, String phone, String email,
            List<String> clinicAddresses, String about
    ) {}

    public record DoctorResponse(
            String id, String fullName, String specialization, String city,
            double rating, BigDecimal consultationFee, String status,
            List<String> clinicAddresses, String about
    ) {}
}
