package com.healthcare.doctor_service.util;

import com.healthcare.doctor_service.dto.DoctorDtos.*;
import com.healthcare.doctor_service.dto.AvailabilityDtos.*;
import com.healthcare.doctor_service.dto.PrescriptionDtos.*;
import com.healthcare.doctor_service.entity.AvailabilitySlot;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.entity.Prescription;
import com.healthcare.doctor_service.entity.*;

public class Mappers {
    public static DoctorResponse toDoctorResponse(Doctor d){
        return new DoctorResponse(
                d.getId(), d.getFullName(), d.getSpecialization(), d.getCity(),
                d.getRating(), d.getConsultationFee(), d.getStatus().name(),
                d.getClinicAddresses(), d.getAbout()
        );
    }
    public static SlotResponse toSlotResponse(AvailabilitySlot s){
        return new SlotResponse(s.getId(), s.getDate(), s.getStartTime(), s.getEndTime(), s.getMode(), s.isBlocked());
    }
    public static PrescriptionResponse toPrescriptionResponse(Prescription p){
        return new PrescriptionResponse(p.getId(), p.getAppointmentId(), p.getPatientId(),
                p.getDoctor().getId(), p.getMedications(), p.getLabTests(), p.getAdvice(), p.getIssuedAt());
    }
}