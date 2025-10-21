package com.healthcare.patient_service.service;

import com.healthcare.patient_service.dto.PatientDtos.*;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.exception.NotFoundException;
import com.healthcare.patient_service.repository.PatientRepository;
import com.healthcare.patient_service.util.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository repo;

    public PatientService(PatientRepository repo){ this.repo = repo; }

    @Transactional
    public Patient create(CreatePatientRequest req){
        // check duplicates by phone/email
        repo.findByPhone(req.phone()).ifPresent(p -> { throw new IllegalArgumentException("Phone already registered"); });
        repo.findByEmail(req.email()).ifPresent(p -> { throw new IllegalArgumentException("Email already registered"); });
        Patient p = Mappers.toEntity(req);
        return repo.save(p);
    }

    public Patient get(Long id){
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Patient not found"));
    }

    @Transactional
    public Patient update(Long id, UpdatePatientRequest req){
        Patient p = get(id);
        if (req.firstName()!=null) p.setFirstName(req.firstName());
        if (req.lastName()!=null) p.setLastName(req.lastName());
        if (req.email()!=null) p.setEmail(req.email());
        if (req.phone()!=null) p.setPhone(req.phone());
        if (req.age()!=null) p.setAge(req.age());
        if (req.gender()!=null) p.setGender(req.gender());
        if (req.address()!=null) p.setAddress(req.address());
        if (req.medicalHistory()!=null) p.setMedicalHistory(req.medicalHistory());
        return repo.save(p);
    }

    public List<Patient> listAll(){ return repo.findAll(); }

    @Transactional
    public void delete(Long id){
        if (!repo.existsById(id)) throw new NotFoundException("Patient not found");
        repo.deleteById(id);
    }

}
