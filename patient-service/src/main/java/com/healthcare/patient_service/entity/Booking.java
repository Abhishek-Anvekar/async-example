package com.healthcare.patient_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bookingId; // UUID or external appointment id
    private Long patientId;
    private String doctorId;
    private LocalDateTime startTime;
    private Integer durationMinutes;
    private String slotId;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    private String notes;
    private LocalDateTime createdAt;
}
