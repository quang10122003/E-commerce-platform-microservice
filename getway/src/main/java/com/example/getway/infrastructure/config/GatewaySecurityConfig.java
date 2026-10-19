package com.example.getway.infrastructure.config;

import com.example.common.security.JwtVerifier;
import com.example.getway.application.port.out.AccessControlCachePort;
import com.example.getway.application.port.in.AuthorizeAccessUseCase;
import com.example.getway.application.port.out.TokenVerificationPort;
import com.example.getway.application.service.AccessControlService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewaySecurityConfig {

    @Bean
    JwtVerifier jwtVerifier(@Value("${app.jwt.public-key}") String publicKey) {
        return new JwtVerifier(publicKey);
    }

    @Bean
    AuthorizeAccessUseCase accessControlService(
            TokenVerificationPort tokenVerificationPort,
            AccessControlCachePort accessControlCachePort) {
        return new AccessControlService(tokenVerificationPort, accessControlCachePort);
    }
}
