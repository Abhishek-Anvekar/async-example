package com.healthcare.doctor_service.service.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DoctorEventsProducer {
    private final KafkaTemplate<String, String> kafka;
    public DoctorEventsProducer(KafkaTemplate<String,String> kafka){
        this.kafka = kafka;
    }

    public void sendDoctorRegistered(String doctorId){
        kafka.send("doctor-registered", doctorId);
    }
    public void sendDoctorVerified(String doctorId){
        kafka.send("doctor-verified", doctorId);
    }
    public void sendAvailabilityUpdated(String doctorId){
        kafka.send("doctor-availability-updated", doctorId);
    }
}
