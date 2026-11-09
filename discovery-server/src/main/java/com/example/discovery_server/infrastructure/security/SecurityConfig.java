package com.example.discovery_server.infrastructure.security;

import com.example.common.security.JwtAuthFilter;
import com.example.common.security.JwtVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    // Service này chỉ quét package của chính nó nên phải khai báo bean dùng chung từ common.
    @Bean
    JwtVerifier jwtVerifier(@Value("${app.jwt.public-key}") String publicKey) {
        return new JwtVerifier(publicKey);
    }

    @Bean
    JwtAuthFilter jwtAuthFilter(JwtVerifier jwtVerifier) {
        return new JwtAuthFilter(jwtVerifier);
    }

    // Eureka client đăng ký và gửi heartbeat không kèm token nên mọi đường dẫn ngoài actuator vẫn phải mở, CSRF phải tắt.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().permitAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
