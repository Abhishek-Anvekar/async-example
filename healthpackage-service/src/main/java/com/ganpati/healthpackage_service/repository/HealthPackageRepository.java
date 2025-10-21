package com.ganpati.healthpackage_service.repository;

import com.ganpati.healthpackage_service.entity.HealthPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthPackageRepository extends JpaRepository<HealthPackage, Long> {}
