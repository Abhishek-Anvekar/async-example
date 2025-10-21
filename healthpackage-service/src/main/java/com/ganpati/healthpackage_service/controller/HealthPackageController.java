package com.ganpati.healthpackage_service.controller;

import com.ganpati.healthpackage_service.entity.HealthPackage;
import com.ganpati.healthpackage_service.service.HealthPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/packages")
public class HealthPackageController {
    @Autowired
    private HealthPackageService service;

    @GetMapping("/{id}")
    public HealthPackage get(@PathVariable Long id){
        Optional<HealthPackage> p = service.findById(id);
        return p.orElse(null);
    }
}
