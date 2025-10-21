package com.healthcare.appointment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient doctorClient(@Value("${external.doctor-service-base-url}") String base) {
        return WebClient.builder().baseUrl(base).build();
    }

    @Bean
    public WebClient patientClient(@Value("${external.patient-service-base-url}") String base) {
        return WebClient.builder().baseUrl(base).build();
    }
}
