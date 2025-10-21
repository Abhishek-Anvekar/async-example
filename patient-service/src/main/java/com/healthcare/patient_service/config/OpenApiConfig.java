package com.healthcare.patient_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Patient Service API", version = "1.0", description = "APIs for patient profile, booking, payments, and reviews"),
        servers = {@Server(url = "http://localhost:8082", description = "Local")}
)
public class OpenApiConfig {}
