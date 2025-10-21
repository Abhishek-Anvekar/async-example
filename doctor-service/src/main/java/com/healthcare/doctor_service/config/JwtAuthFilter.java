//package com.javaguides.doctor_service.config;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//
//@Component
//public class JwtAuthFilter extends OncePerRequestFilter {
//    private final byte[] key = System.getenv().getOrDefault("JWT_SECRET","change-me-to-long-secret").getBytes(StandardCharsets.UTF_8);
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws ServletException, IOException {
//        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
//        if (auth != null && auth.startsWith("Bearer ")) {
//            try {
//                var parser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor(key)).build();
//                var claims = parser.parseSignedClaims(auth.substring(7)).getPayload();
//                String subject = claims.getSubject();
//                Object rolesObj = claims.get("roles");
//                List<SimpleGrantedAuthority> auths = new ArrayList<>();
//                if (rolesObj instanceof Collection<?> col) {
//                    for (Object r : col) auths.add(new SimpleGrantedAuthority("ROLE_" + r.toString().replace("ROLE_","")));
//                } else if (rolesObj instanceof String s) {
//                    for (String r : s.split(",")) auths.add(new SimpleGrantedAuthority(r.startsWith("ROLE_")? r : "ROLE_"+r.trim()));
//                }
//                var authentication = new AbstractAuthenticationToken(auths) {
//                    @Override public Object getCredentials() { return ""; }
//                    @Override public Object getPrincipal() { return subject; }
//                };
//                authentication.setAuthenticated(true);
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            } catch (Exception e) {
//                // ignore invalid token -> anonymous
//            }
//        }
//        chain.doFilter(req, res);
//    }
//}
