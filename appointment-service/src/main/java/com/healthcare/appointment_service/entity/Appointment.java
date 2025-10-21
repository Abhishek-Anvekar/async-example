package com.healthcare.appointment_service.entity;

import com.healthcare.appointment_service.dto.AppointmentDtos.AppointmentStatus;
import com.healthcare.appointment_service.dto.AppointmentDtos.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments", indexes = {
        @Index(name="idx_appt_doctor_time", columnList="doctorId,startTime"),
        @Index(name="idx_appt_patient", columnList="patientId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @Column(length = 40)
    private String id; // ULID/UUID

    @Column(nullable = false)
    private String doctorId;

    @Column(nullable = false)
    private Long patientId;

    @Column(nullable = false)
    private String patientPhone;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private String notes;
    private String slotId; // from doctor-service (optional)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
