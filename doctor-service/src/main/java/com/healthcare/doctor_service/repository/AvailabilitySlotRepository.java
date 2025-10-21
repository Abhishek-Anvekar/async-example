package com.healthcare.doctor_service.repository;


import com.healthcare.doctor_service.entity.AvailabilitySlot;
import com.healthcare.doctor_service.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, String> {
    List<AvailabilitySlot> findByDoctorAndDateOrderByStartTime(Doctor doctor, LocalDate date);
    List<AvailabilitySlot> findByDoctorIdOrderByDateAscStartTimeAsc(String doctorId);
}
