package com.healthcare.doctor_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Doctor Service API",
                version = "1.0",
                description = "APIs for managing doctor profiles, appointments, prescriptions, and reviews"
        ),
        servers = {
                @Server(url = "http://localhost:8083", description = "Local Dev Server"),
                @Server(url = "https://api.healthcare.com", description = "Production Server")
        }
)
/**
 * NOTE: This require when you implementing JWT here
 */
//@SecurityScheme(
//        name = "bearerAuth",
//        type = SecuritySchemeType.HTTP,
//        scheme = "bearer",
//        bearerFormat = "JWT"
//)
public class OpenApiConfig {
}
