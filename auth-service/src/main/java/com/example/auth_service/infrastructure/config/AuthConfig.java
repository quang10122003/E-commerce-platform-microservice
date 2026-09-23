package com.example.auth_service.infrastructure.config;

import com.example.auth_service.application.port.in.ChecktokenUseCase;
import com.example.auth_service.application.port.in.LoginUseCase;
import com.example.auth_service.application.port.in.LogoutUserCase;
import com.example.auth_service.application.port.in.RefreshTokenUseCase;
import com.example.auth_service.application.port.in.RegisterUseCase;
import com.example.auth_service.application.port.out.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.auth_service.application.service.AuthApplicationService;
import com.example.auth_service.application.service.AuthServiceInternal;
import com.example.auth_service.application.port.in.InternalGetUserInfoUseCase;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;
import com.example.auth_service.application.registry.OutboxEventHandlerRegistry;
import com.example.auth_service.application.strategy.outbox.OutboxEventHandler;
import com.example.auth_service.application.strategy.outbox.UserRegisteredOutboxHandler;

import java.util.List;

@Configuration
public class AuthConfig {
    // Đăng ký strategy xử lý event UserRegistered qua port phát hành message.
    @Bean
    OutboxEventHandler userRegisteredOutboxHandler(
            AuthEventPublisherPort authEventPublisherPort
    ) {
        return new UserRegisteredOutboxHandler(authEventPublisherPort);
    }

    // Gom các strategy outbox để định tuyến theo eventType.
    @Bean
    OutboxEventHandlerRegistry outboxEventHandlerRegistry(
            List<OutboxEventHandler> eventHandlers
    ) {
        return new OutboxEventHandlerRegistry(eventHandlers);
    }

    @Bean
    AuthApplicationService authApplicationService(AuthenticationManagerPort authenticationManagerPort, TokenServicePort tokenServicePort, RoleRepositoryPort roleRepositoryPort, UserRepositoryPort userRepositoryPort, OutboxEventPort outboxEventPort, AccessControlCachePort accessControlCachePort){
        return new AuthApplicationService(authenticationManagerPort, tokenServicePort,roleRepositoryPort,userRepositoryPort, outboxEventPort,accessControlCachePort);
    }

    @Bean
    InternalGetUserInfoUseCase internalGetUserInfoUseCase(TokenServicePort tokenServicePort) {
        return new AuthServiceInternal(tokenServicePort);
    }

    @Bean
    LoginUseCase loginUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

    @Bean
    RegisterUseCase registerUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

    @Bean
    RefreshTokenUseCase refreshTokenUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

    @Bean
    ChecktokenUseCase checktokenUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

    @Bean
    LogoutUserCase logoutUserCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

}
