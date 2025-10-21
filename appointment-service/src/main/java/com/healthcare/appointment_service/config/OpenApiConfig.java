package com.healthcare.appointment_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Appointment Service API", version = "1.0", description = "Booking, reschedule, cancel, and lifecycle APIs"),
        servers = {@Server(url = "http://localhost:8084", description = "Local")}
)
public class OpenApiConfig { }
