package com.healthcare.patient_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${external.doctor-service-base-url}")
    private String doctorBase;

    @Value("${external.appointment-service-base-url}")
    private String appointmentBase;

    @Bean
    public WebClient doctorWebClient() {
        return WebClient.builder().baseUrl(doctorBase).build();
    }

    @Bean
    public WebClient appointmentWebClient() {
        return WebClient.builder().baseUrl(appointmentBase).build();
    }
}
