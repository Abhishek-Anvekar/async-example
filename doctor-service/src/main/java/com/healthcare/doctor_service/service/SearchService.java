package com.healthcare.doctor_service.service;

import com.healthcare.doctor_service.dto.BulkResponse;
import com.healthcare.doctor_service.entity.Doctor;
import com.healthcare.doctor_service.exception.NotFoundException;
import com.healthcare.doctor_service.repository.DoctorRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.*;

@Service
public class SearchService {
    private final DoctorRepository repo;
    private final WebClient reviewClient;
    private final WebClient appointmentClient;

    public SearchService(DoctorRepository repo, WebClient reviewClient, WebClient appointmentClient) {
        this.repo = repo; this.reviewClient = reviewClient; this.appointmentClient = appointmentClient;
    }

    public List<Map<String,Object>> upcomingAppointmentsForDoctor(String doctorId){
        // Calls appointment-service: GET /appointments/doctor/{doctorId}/upcoming
        BulkResponse<Map<String,Object>> response = appointmentClient.get()
                .uri("/appointments/doctor/{doctorId}/upcoming", doctorId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BulkResponse<Map<String,Object>>>() {})
                .onErrorResume(e -> Mono.just(new BulkResponse<>(List.of()))) // empty list on error
                .block();

        // Return the list inside BulkResponse
        return response.getItems();
    }

//    public List<Map<String, Object>> pastAppointmentsForDoctor(String doctorId) {
//        // Calls appointment-service: GET /appointments/doctor/{doctorId}/past
//        return appointmentClient.get()
//                .uri("/appointments/doctor/{doctorId}/past", doctorId)
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
//                .onErrorResume(e -> Mono.just(List.of()))
//                .block();
//    }

    public List<Map<String,Object>> pastAppointmentsForDoctor(String doctorId) {
        BulkResponse<Map<String, Object>> response = appointmentClient.get()
                .uri("/appointments/doctor/{doctorId}/past", doctorId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BulkResponse<Map<String,Object>>>() {})
                .onErrorResume(e -> Mono.just(new BulkResponse<>(List.of())))
                .block();

        return response.getItems(); // <-- extract the actual list
    }

    public List<Map<String,Object>> reviewsForDoctor(String doctorId){
        // Calls review-service: GET /reviews/doctor/{doctorId}
        return reviewClient.get()
                .uri("/reviews/doctor/{doctorId}", doctorId)
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<Map<String,Object>>>(){}).onErrorResume(e-> Mono.just(List.of()))
                .block();
    }

    public double refreshRatingFromReviews(String doctorId){
        List<Map<String,Object>> reviews = reviewsForDoctor(doctorId);
        if (reviews.isEmpty()) return 0.0;
        double avg = reviews.stream().mapToInt(r -> (int) r.getOrDefault("rating", 0)).average().orElse(0.0);
        Doctor d = repo.findById(doctorId).orElseThrow(() -> new NotFoundException("Doctor not found"));
        d.setRating(avg);
        repo.save(d);
        return avg;
    }
}
