package com.healthcare.appointment_service.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Component
public class ExternalServiceClient {

    private final WebClient doctorClient;
    private final WebClient patientClient;

    public ExternalServiceClient(WebClient doctorClient, WebClient patientClient){
        this.doctorClient = doctorClient;
        this.patientClient = patientClient;
    }

    @Async("taskExecutor")
    public CompletableFuture<Map<String,Object>> getDoctorProfileAsync(String doctorId){
        return doctorClient.get()
                .uri("/doctors/{id}", doctorId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(e -> Mono.just(Map.of())) // return empty map on error
                .toFuture();
    }

    @Async("taskExecutor")
    public CompletableFuture<Map<String,Object>> getPatientAsync(Long patientId){
        return patientClient.get().uri("/patients/{id}", patientId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                .onErrorResume(e-> Mono.just(Map.of()))
                .toFuture();
    }

    /**
     * Validates a slot/time with doctor-service and (optionally) "locks" it by blocking it for booking.
     * If you already have slotId, you can call the block API; else validate by checking free slots.
     */
    public boolean validateAndLockSlot(String doctorId, String slotId, LocalDateTime start, int durationMinutes){
        try {
            // If slotId provided → call block API (toggle blocked=true for that slot)
            if (slotId != null && !slotId.isBlank()) {
                Map<String,Object> req = Map.of("slotIds", List.of(slotId), "blocked", true);
                Map resp = doctorClient.put()
                        .uri("/doctors/{id}/availability/slots/block", doctorId)
                        .bodyValue(req).retrieve().bodyToMono(Map.class).block();
                return resp != null;
            } else {
                // else fetch all slots and ensure start is available (simple check)
                List<Map<String,Object>> slots = doctorClient.get()
                        .uri("/doctors/{id}/availability/slots", doctorId)
                        .retrieve().bodyToMono(List.class).block();
                if (slots == null) return false;
                return slots.stream().anyMatch(s -> {
                    String sStart = String.valueOf(s.get("startTime"));
                    boolean blocked = Boolean.TRUE.equals(s.get("blocked"));
                    Integer dur = Integer.valueOf(String.valueOf(s.get("durationMinutes")));
                    return sStart.equals(start.toString()) && dur == durationMinutes && !blocked;
                });
            }
        } catch (Exception e){
            return false;
        }
    }

    public void unlockSlotIfNeeded(String doctorId, String slotId){
        if (slotId == null || slotId.isBlank()) return;
        try {
            Map<String,Object> req = Map.of("slotIds", List.of(slotId), "blocked", false);
            doctorClient.put()
                    .uri("/doctors/{id}/availability/slots/block", doctorId)
                    .bodyValue(req).retrieve().bodyToMono(Map.class).block();
        } catch (Exception ignored) { }
    }
}
