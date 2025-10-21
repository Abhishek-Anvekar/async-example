package com.healthcare.appointment_service.repository;

import com.healthcare.appointment_service.entity.Appointment;
import com.healthcare.appointment_service.dto.AppointmentDtos.AppointmentStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    @Query("select a from Appointment a where a.doctorId = :doctorId and a.startTime < :end and a.startTime >= :start")
    List<Appointment> findDoctorAppointmentsBetween(String doctorId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByDoctorIdAndStatusInOrderByStartTimeAsc(String doctorId, List<AppointmentStatus> statuses);

    //past appointments for doctor
    List<Appointment> findByDoctorIdAndStatusInOrderByStartTimeDesc(String doctorId, List<AppointmentStatus> statuses);

    //past appointments for patient
    List<Appointment> findByPatientIdAndStatusInOrderByStartTimeDesc(Long patientId, List<AppointmentStatus> statuses);

    List<Appointment> findByPatientIdOrderByStartTimeDesc(Long patientId);

    Optional<Appointment> findFirstByDoctorIdAndStartTimeAndStatusIn(String doctorId, LocalDateTime start, List<AppointmentStatus> statuses);

    List<Appointment> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}
