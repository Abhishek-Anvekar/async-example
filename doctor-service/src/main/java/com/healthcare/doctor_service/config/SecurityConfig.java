//package com.javaguides.doctor_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//public class SecurityConfig {
//    private final JwtAuthFilter jwtAuthFilter;
//    public SecurityConfig(JwtAuthFilter jwtAuthFilter){ this.jwtAuthFilter = jwtAuthFilter; }
//
//    @Bean
//    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/actuator/**","/v3/api-docs/**",
//                                "/swagger-ui.html",
//                                "/swagger-ui/**",
//                                "/swagger-resources/**",
//                                "/webjars/**").permitAll()
//                        .requestMatchers("/doctors/register").hasAnyRole("ADMIN") // Admin registers/approves
//                        .requestMatchers("/doctors/**/verify").hasRole("ADMIN")
//                        .requestMatchers("/doctors/**/availability/**").hasRole("DOCTOR")
//                        .requestMatchers("/doctors/**/prescriptions/**").hasRole("DOCTOR")
//                        .requestMatchers("/doctors/**/appointments/**").hasAnyRole("DOCTOR")
//                        .anyRequest().permitAll()
//                )
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .httpBasic(Customizer.withDefaults());
//        return http.build();
//    }
//}
