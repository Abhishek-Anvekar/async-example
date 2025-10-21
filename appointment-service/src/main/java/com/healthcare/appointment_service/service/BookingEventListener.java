package com.healthcare.appointment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.appointment_service.dto.AppointmentDtos.BookAppointmentRequest;
import com.healthcare.appointment_service.entity.Appointment;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class BookingEventListener {

    private final AppointmentDomainService service;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${kafka.topics.bookRequest}")
    private String topicBook;

    public BookingEventListener(AppointmentDomainService service) {
        this.service = service;
    }

    @KafkaListener(topics = "${kafka.topics.bookRequest}", groupId = "appointment-service")
    public void onBookRequest(String message) {
        try {
            Map<String, Object> map = mapper.readValue(message, Map.class);
            String doctorId = (String) map.get("doctorId");
            Long patientId = Long.valueOf(String.valueOf(map.get("patientId")));
            LocalDateTime startTime = LocalDateTime.parse(String.valueOf(map.get("startTime")));
            Integer duration = Integer.valueOf(String.valueOf(map.get("durationMinutes")));
            @Nullable String slotId = map.get("slotId") != null ? String.valueOf(map.get("slotId")) : null;
            String notes = (String) map.getOrDefault("notes", null);

            BookAppointmentRequest req = new BookAppointmentRequest(doctorId, patientId, startTime, duration, slotId, notes);
            Appointment a = service.book(req);
            // Booking created; confirmation still requires doctor/auto confirmation flow (up to you).
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
