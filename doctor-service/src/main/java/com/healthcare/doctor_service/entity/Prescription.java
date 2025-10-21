package com.healthcare.doctor_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="prescriptions")
@Getter
@Setter
@NoArgsConstructor
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional=false)
    @JoinColumn(name="doctor_id")
    private Doctor doctor;

    @Column(nullable=false)
    private String appointmentId;
    @Column(nullable=false)
    private String patientId;

    @ElementCollection
    @CollectionTable(name="prescription_meds", joinColumns=@JoinColumn(name="prescription_id"))
    @Column(name="med")
    private List<String> medications = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name="prescription_tests", joinColumns=@JoinColumn(name="prescription_id"))
    @Column(name="test")
    private List<String> labTests = new ArrayList<>();

    @Column(length=2000) private String advice;

    private OffsetDateTime issuedAt = OffsetDateTime.now();
}
