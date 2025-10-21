package com.healthcare.doctor_service.repository;

import com.healthcare.doctor_service.entity.Doctor;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String>, JpaSpecificationExecutor<Doctor> {
    boolean existsByLicenseNumber(String licenseNumber);
}