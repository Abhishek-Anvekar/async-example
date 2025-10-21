package com.healthcare.doctor_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient reviewClient() {
        String base = System.getenv().getOrDefault("REVIEW_BASE_URL","http://localhost:8088");
        return WebClient.builder().baseUrl(base).build();
    }

    @Bean
    public WebClient appointmentClient() {
        String base = System.getenv().getOrDefault("APPOINTMENT_BASE_URL","http://localhost:8084");
        return WebClient.builder().baseUrl(base).build();
    }

}
