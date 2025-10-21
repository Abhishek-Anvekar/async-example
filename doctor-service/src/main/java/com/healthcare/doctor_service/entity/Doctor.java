package com.healthcare.doctor_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "doctors")
@Getter @Setter @NoArgsConstructor
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String specialization;
    @Column(nullable = false)
    private String city;

    private double rating; // computed from review-service
    private BigDecimal consultationFee;
    private String phone;
    private String email;
    @Column(unique = true, nullable = false)
    private String licenseNumber;

    @ElementCollection
    @CollectionTable(name = "doctor_clinics", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "address")
    private List<String> clinicAddresses = new ArrayList<>();

    @Column(length = 2000)
    private String about;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING; // PENDING, APPROVED, ACTIVE, INACTIVE

    public enum Status { PENDING, APPROVED, ACTIVE, INACTIVE }
}
