package com.healthcare.patient_service.util;

import com.healthcare.patient_service.dto.PatientDtos.*;
import com.healthcare.patient_service.entity.Patient;

public class Mappers {

    public static PatientResponse toResponse(Patient p){
        if (p==null) return null;
        return new PatientResponse(p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(),
                p.getPhone(), p.getAge(), p.getGender(), p.getAddress(), p.getMedicalHistory());
    }

    public static Patient toEntity(CreatePatientRequest req){
        return Patient.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .phone(req.phone())
                .age(req.age())
                .gender(req.gender())
                .build();
    }
}
