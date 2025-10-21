package com.healthcare.patient_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patient_documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long patientId;
    private String filename;
    private String contentType;
    @Lob @Basic(fetch = FetchType.LAZY)
    private byte[] content;
}
