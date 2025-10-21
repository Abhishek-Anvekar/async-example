package com.healthcare.doctor_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "availability_slots",
        uniqueConstraints = @UniqueConstraint(columnNames={"doctor_id","date","startTime","endTime"}))
@Getter
@Setter
@NoArgsConstructor
public class AvailabilitySlot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional=false)
    @JoinColumn(name="doctor_id")
    private Doctor doctor;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @Column(nullable=false)
    private String mode; // ONLINE | CLINIC | HOME
    private boolean blocked = false;
    @Version
    private Long version; // 👈 Optimistic locking. This is the key for optimistic locking
}
