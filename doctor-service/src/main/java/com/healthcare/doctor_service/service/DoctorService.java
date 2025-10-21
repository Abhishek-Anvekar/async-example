package com.healthcare.doctor_service.service;

import com.healthcare.doctor_service.dto.DoctorDtos.*;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.exception.BadRequestException;
import com.healthcare.doctor_service.exception.NotFoundException;
import com.healthcare.doctor_service.exception.*;
import com.healthcare.doctor_service.repository.DoctorRepository;
import com.healthcare.doctor_service.service.messaging.DoctorEventsProducer;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorService {
    private final DoctorRepository repo;
    private final DoctorEventsProducer producer;

    public DoctorService(DoctorRepository repo, DoctorEventsProducer producer){
        this.repo = repo; this.producer = producer;
    }

    @Transactional
    public Doctor register(RegisterDoctorRequest req){
        if (repo.existsByLicenseNumber(req.licenseNumber()))
            throw new BadRequestException("License already registered");
        Doctor d = new Doctor();
        d.setFullName(req.fullName());
        d.setSpecialization(req.specialization());
        d.setCity(req.city());
        d.setConsultationFee(req.consultationFee());
        d.setPhone(req.phone());
        d.setEmail(req.email());
        d.setLicenseNumber(req.licenseNumber());
        if (req.clinicAddresses()!=null) d.getClinicAddresses().addAll(req.clinicAddresses());
        d.setStatus(Doctor.Status.PENDING);
        repo.save(d);
        producer.sendDoctorRegistered(d.getId());
        return d;
    }

    @Transactional
    public Doctor verify(String doctorId, boolean approve) {
        Doctor d = repo.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
        d.setStatus(approve ? Doctor.Status.APPROVED : Doctor.Status.INACTIVE);
        repo.save(d);
        if (approve) producer.sendDoctorVerified(d.getId());
        return d;
    }

    @Transactional
    public Doctor activate(String doctorId, boolean active){
        Doctor d = repo.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
        if (d.getStatus() == Doctor.Status.PENDING) throw new BadRequestException("Doctor not verified");
        d.setStatus(active ? Doctor.Status.ACTIVE : Doctor.Status.INACTIVE);
        return repo.save(d);
    }

//    @Transactional
//    public Doctor updateProfile(String doctorId, UpdateDoctorProfileRequest req, String requester, boolean isDoctor){
//        Doctor d = repo.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
//        if (isDoctor && !Objects.equals(d.getId(), requester))
//            throw new ForbiddenException("Cannot update other doctor's profile");
//
//        if (req.fullName()!=null) d.setFullName(req.fullName());
//        if (req.specialization()!=null) d.setSpecialization(req.specialization());
//        if (req.city()!=null) d.setCity(req.city());
//        if (req.consultationFee()!=null) d.setConsultationFee(req.consultationFee());
//        if (req.phone()!=null) d.setPhone(req.phone());
//        if (req.email()!=null) d.setEmail(req.email());
//        if (req.clinicAddresses()!=null) { d.getClinicAddresses().clear(); d.getClinicAddresses().addAll(req.clinicAddresses()); }
//        if (req.about()!=null) d.setAbout(req.about());
//
//        return repo.save(d);
//    }

    @Transactional
    public Doctor updateProfile(String doctorId, UpdateDoctorProfileRequest req) {
        Doctor d = repo.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        if (req.fullName() != null) d.setFullName(req.fullName());
        if (req.specialization() != null) d.setSpecialization(req.specialization());
        if (req.city() != null) d.setCity(req.city());
        if (req.rating() != 0.0) d.setRating(req.rating());
        if (req.consultationFee() != null) d.setConsultationFee(req.consultationFee());
        if (req.phone() != null) d.setPhone(req.phone());
        if (req.email() != null) d.setEmail(req.email());
        if (req.clinicAddresses() != null) {
            d.getClinicAddresses().clear();
            d.getClinicAddresses().addAll(req.clinicAddresses());
        }
        if (req.about() != null) d.setAbout(req.about());

        return repo.save(d);
    }

    public Page<Doctor> search(String name, String specialization, String city, Double minRating, Pageable pageable) {
        Specification<Doctor> spec = Specification.where(null);
        if (name != null && !name.isBlank())
            spec = spec.and((r,q,cb)-> cb.like(cb.lower(r.get("fullName")), "%"+name.toLowerCase()+"%"));
        if (specialization != null && !specialization.isBlank())
            spec = spec.and((r,q,cb)-> cb.equal(cb.lower(r.get("specialization")), specialization.toLowerCase()));
        if (city != null && !city.isBlank())
            spec = spec.and((r,q,cb)-> cb.equal(cb.lower(r.get("city")), city.toLowerCase()));
        if (minRating != null)
            spec = spec.and((r,q,cb)-> cb.greaterThanOrEqualTo(r.get("rating"), minRating));
        return repo.findAll(spec, pageable);
    }

    public Doctor get(String id){
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Doctor not found"));
    }
}
