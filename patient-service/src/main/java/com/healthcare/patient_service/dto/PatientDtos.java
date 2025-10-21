package com.healthcare.patient_service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * NOTE:
 * In Java, a record is already an immutable data class with its own
 * constructor, getters, equals(), hashCode(), and toString().
 *
 * The outer class PatientDtos doesn’t have any fields, but Lombok annotations
 * like @NoArgsConstructor and @AllArgsConstructor still try to generate constructors,
 * which leads to duplicate constructor definitions.
 *
 * ✅ Solution: Remove Lombok annotations from this wrapper class, or
 * split DTOs into separate Lombok-based classes instead of using records.
 */

//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientDtos {

    public static record CreatePatientRequest(
            @NotBlank String firstName,
            String lastName,
            @Email String email,
            @NotBlank String phone,
            Integer age,
            String gender
    ) {}

    public static record UpdatePatientRequest(
            String firstName,
            String lastName,
            @Email String email,
            String phone,
            Integer age,
            String gender,
            String address,
            String medicalHistory
    ) {}

    public static record PatientResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            Integer age,
            String gender,
            String address,
            String medicalHistory
    ) {}
}
