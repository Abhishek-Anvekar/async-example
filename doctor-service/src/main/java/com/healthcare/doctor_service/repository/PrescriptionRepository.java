package com.healthcare.doctor_service.repository;

import com.healthcare.doctor_service.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
    List<Prescription> findByDoctorIdOrderByIssuedAtDesc(String doctorId);
    List<Prescription> findByPatientIdOrderByIssuedAtDesc(String patientId);
}
