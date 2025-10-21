package com.ganpati.healthpackage_service.service;

import com.ganpati.healthpackage_service.entity.HealthPackage;
import com.ganpati.healthpackage_service.repository.HealthPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HealthPackageService {
    @Autowired
    private HealthPackageRepository repo;

    public Optional<HealthPackage> findById(Long id){
        return repo.findById(id);
    }
}
