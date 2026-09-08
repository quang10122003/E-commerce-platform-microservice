package com.example.getway.infrastructure.config;

import com.example.common.security.JwtVerifier;
import com.example.getway.application.port.out.AccessControlCachePort;
import com.example.getway.application.port.out.TokenVerificationPort;
import com.example.getway.application.service.AccessControlService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Cấu hình các dependency security của Gateway ở infrastructure layer.
@Configuration
public class GatewaySecurityConfig {

    // Tạo bộ verify JWT bằng public key của auth-service.
    @Bean
    JwtVerifier jwtVerifier(@Value("${app.jwt.public-key}") String publicKey) {
        return new JwtVerifier(publicKey);
    }

    // Tạo application service và chỉ nối với các port cần thiết.
    @Bean
    AccessControlService accessControlService(
            TokenVerificationPort tokenVerificationPort,
            AccessControlCachePort accessControlCachePort) {
        return new AccessControlService(tokenVerificationPort, accessControlCachePort);
    }
}
