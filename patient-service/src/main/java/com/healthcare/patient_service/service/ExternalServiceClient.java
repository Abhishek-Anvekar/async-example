package com.healthcare.patient_service.service;

import com.healthcare.patient_service.dto.BulkResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class ExternalServiceClient {

    private final WebClient doctorClient;
    private final WebClient appointmentClient;

    public ExternalServiceClient(WebClient doctorWebClient, WebClient appointmentWebClient){
        this.doctorClient = doctorWebClient;
        this.appointmentClient = appointmentWebClient;
    }

    public List<Map<String,Object>> fetchDoctorAvailability(String doctorId){
        return doctorClient.get()
                .uri("/doctors/{id}/availability/slots", doctorId)
                .retrieve()
                .bodyToMono(List.class)
                .onErrorResume(e -> Mono.just(List.of()))
                .block();
    }

    public Map<String,Object> fetchDoctorProfile(String doctorId){
        return doctorClient.get().uri("/doctors/{id}", doctorId).retrieve().bodyToMono(Map.class).block();
    }

    public List<Map<String,Object>> fetchDoctorReviews(String doctorId){
        return doctorClient.get().uri("/doctors/{id}/reviews", doctorId).retrieve().bodyToMono(List.class).block();
    }

    // appointment history or upcoming can be called similarly from appointmentClient

//    public List<Map<String, Object>> pastAppointmentsForPatient(Long patientId) {
//        // Calls appointment-service: GET /appointments/patient/{patientId}/past
//        return appointmentClient.get()
//                .uri("/appointments/patient/{patientId}/past", patientId)
//                .retrieve()
//                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
//                .onErrorResume(e -> Mono.just(List.of()))
//                .block();
//    }

    public List<Map<String,Object>> pastAppointmentsForPatient(Long patientId) {
        // Calls appointment-service: GET /appointments/patient/{patientId}/past
        BulkResponse<Map<String, Object>> response = appointmentClient.get()
                .uri("/appointments/patient/{patientId}/past", patientId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<BulkResponse<Map<String,Object>>>() {})
                .onErrorResume(e -> Mono.just(new BulkResponse<>(List.of())))
                .block();

        return response.getItems();
    }
}
