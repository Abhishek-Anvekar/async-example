package com.healthcare.doctor_service.service;

import com.healthcare.doctor_service.dto.PrescriptionDtos.CreatePrescriptionRequest;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.entity.Prescription;
import com.healthcare.doctor_service.exception.BadRequestException;
import com.healthcare.doctor_service.exception.NotFoundException;
import com.healthcare.doctor_service.repository.DoctorRepository;
import com.healthcare.doctor_service.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrescriptionService {
    private final PrescriptionRepository repo;
    private final DoctorRepository doctorRepo;

    public PrescriptionService(PrescriptionRepository repo, DoctorRepository doctorRepo) {
        this.repo = repo; this.doctorRepo = doctorRepo;
    }

    @Transactional
    public Prescription create(String doctorId, CreatePrescriptionRequest req){
        if (req.medications()==null || req.medications().isEmpty())
            throw new BadRequestException("Medications required");
        Doctor d = doctorRepo.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
        Prescription p = new Prescription();
        p.setDoctor(d);
        p.setAppointmentId(req.appointmentId());
        p.setPatientId(req.patientId());
        p.getMedications().addAll(req.medications());
        if (req.labTests()!=null) p.getLabTests().addAll(req.labTests());
        p.setAdvice(req.advice());
        return repo.save(p);
    }

    public List<Prescription> byDoctor(String doctorId){
        return repo.findByDoctorIdOrderByIssuedAtDesc(doctorId);
    }

    public List<Prescription> byPatient(String patientId){
        return repo.findByPatientIdOrderByIssuedAtDesc(patientId);
    }
}