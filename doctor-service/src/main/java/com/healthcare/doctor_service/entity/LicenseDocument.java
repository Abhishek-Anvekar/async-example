package com.healthcare.doctor_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="license_documents")
@Getter
@Setter
@NoArgsConstructor
public class LicenseDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(optional=false)
    @JoinColumn(name="doctor_id")
    private Doctor doctor;

    @Column(nullable=false)
    private String fileName;
    @Column(nullable=false)
    private String contentType;
    @Lob
    @Basic(fetch=FetchType.LAZY)
    private byte[] content;
}
