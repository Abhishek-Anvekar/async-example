package com.healthcare.patient_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    @Column(unique = true) private String email;
    @Column(unique = true) private String phone;
    private Integer age;
    private String gender;
    @Column(length = 2000) private String address;
    @Column(length = 4000) private String medicalHistory;
}
