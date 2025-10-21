package com.healthcare.patient_service.repository;

import com.healthcare.patient_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByPatientIdOrderByStartTimeDesc(Long patientId);
    List<Booking> findByDoctorIdAndStatus(String doctorId, String status);
    Optional<Booking> findByBookingId(String bookingId);
}
