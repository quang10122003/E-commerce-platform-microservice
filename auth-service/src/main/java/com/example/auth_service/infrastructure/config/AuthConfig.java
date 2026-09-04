package com.example.auth_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.auth_service.application.port.in.LoginUseCase;
import com.example.auth_service.application.port.in.RegisterUseCase;
import com.example.auth_service.application.port.out.AuthenticationManagerPort;
import com.example.auth_service.application.port.out.OutboxEventPort;
import com.example.auth_service.application.port.out.RoleRepositoryPort;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.application.service.AuthApplicationService;
import com.example.common.untill.ValidationUtils;

@Configuration
public class AuthConfig {
    @Bean
    AuthApplicationService authApplicationService(AuthenticationManagerPort authenticationManagerPort, TokenServicePort tokenServicePort, ValidationUtils validationUtils,RoleRepositoryPort roleRepositoryPort, UserRepositoryPort userRepositoryPort, OutboxEventPort outboxEventPort){
        return new AuthApplicationService(authenticationManagerPort, tokenServicePort, validationUtils,roleRepositoryPort,userRepositoryPort, outboxEventPort);
    }

    @Bean
    LoginUseCase loginUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

    @Bean
    RegisterUseCase registerUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }
    
}
