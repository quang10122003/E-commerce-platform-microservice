package com.example.auth_service.infrastructure.config;

import com.example.auth_service.application.port.out.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.auth_service.application.service.AuthApplicationService;

@Configuration
public class AuthConfig {
    @Bean
    AuthApplicationService authApplicationService(AuthenticationManagerPort authenticationManagerPort, TokenServicePort tokenServicePort, RoleRepositoryPort roleRepositoryPort, UserRepositoryPort userRepositoryPort, OutboxEventPort outboxEventPort, AccessControlCachePort accessControlCachePort){
        return new AuthApplicationService(authenticationManagerPort, tokenServicePort,roleRepositoryPort,userRepositoryPort, outboxEventPort,accessControlCachePort);
    }

//    @Bean
//    LoginUseCase loginUseCase(AuthApplicationService authApplicationService) {
//        return authApplicationService;
//    }
//
//    @Bean
//    RegisterUseCase registerUseCase(
//            AuthApplicationService authApplicationService,
//            TransactionTemplate transactionTemplate) {
//        // Bao bọc use case đăng ký bằng transaction tại tầng infrastructure.
//        return request -> transactionTemplate.execute(
//                status -> authApplicationService.register(request));
//    }
    
}
