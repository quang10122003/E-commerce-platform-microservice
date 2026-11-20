package com.example.auth_service.infrastructure.config;

import com.example.auth_service.application.port.in.DeleteStoredFilesUseCase;
import com.example.auth_service.application.port.out.PayloadMapperPort;
import com.example.auth_service.application.port.out.storage.FileStoragePort;
import com.example.auth_service.application.port.out.storage.StoredFileRollbackPort;
import com.example.auth_service.application.service.DeleteStoredFilesService;
import com.example.auth_service.application.service.ShopProfileService;
import com.example.auth_service.application.strategy.outbox.StoredFilesDeleteOutboxHandler;
import com.example.auth_service.application.port.out.OrderActivityPort;
import com.example.auth_service.application.strategy.outbox.UserDeletedOutboxHandler;
import com.example.auth_service.application.port.in.ChecktokenUseCase;
import com.example.auth_service.application.port.in.LoginUseCase;
import com.example.auth_service.application.port.in.LogoutUserCase;
import com.example.auth_service.application.port.in.ChangePasswordUseCase;
import com.example.auth_service.application.port.in.RefreshTokenUseCase;
import com.example.auth_service.application.port.in.RegisterUseCase;
import com.example.auth_service.application.port.in.ProcessAuthOutboxUseCase;
import com.example.auth_service.application.port.out.*;
import com.example.auth_service.application.port.out.cache.CachePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.auth_service.application.service.AuthApplicationService;
import com.example.auth_service.application.service.AuthOutboxStatusService;
import com.example.auth_service.application.service.AuthServiceInternal;
import com.example.auth_service.application.service.UserService;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;
import com.example.auth_service.application.registry.OutboxEventHandlerRegistry;
import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.strategy.EventHandler;
import com.example.auth_service.application.strategy.outbox.UserRegisteredOutboxHandler;

import com.example.auth_service.application.port.in.ExchangeOAuthLoginCodeUseCase;
import com.example.auth_service.application.registry.OAuthLoginHandlerRegistry;
import com.example.auth_service.application.service.OAuthLoginService;
import com.example.auth_service.application.strategy.oauth.GoogleOAuthLoginHandler;
import com.example.auth_service.application.strategy.oauth.OAuthLoginHandler;

import java.util.List;
import java.time.Clock;

@Configuration
public class AuthConfig {
    @Bean
    EventHandler<OutboxEventDto> userRegisteredOutboxHandler(
            AuthEventPublisherPort authEventPublisherPort
    ) {
        return new UserRegisteredOutboxHandler(authEventPublisherPort);
    }

    @Bean
    OutboxEventHandlerRegistry outboxEventHandlerRegistry(
            List<EventHandler<OutboxEventDto>> eventHandlers
    ) {
        return new OutboxEventHandlerRegistry(eventHandlers);
    }

    @Bean
    ProcessAuthOutboxUseCase processAuthOutboxUseCase(
            OutboxEventPort outboxEventPort,
            OutboxEventHandlerRegistry outboxEventHandlerRegistry
    ) {
        return new AuthOutboxStatusService(outboxEventPort, outboxEventHandlerRegistry);
    }

    @Bean
    AuthApplicationService authApplicationService(AuthenticationManagerPort authenticationManagerPort,
            TokenServicePort tokenServicePort, RoleRepositoryPort roleRepositoryPort,
            UserRepositoryPort userRepositoryPort, OutboxEventPort outboxEventPort, CachePort cachePort, Clock clock) {
        return new AuthApplicationService(authenticationManagerPort, tokenServicePort, roleRepositoryPort,
                userRepositoryPort, outboxEventPort, cachePort, clock);
    }

    @Bean(name = {"userService", "getUsersUseCase", "getUserOverviewUseCase", "toggleUserLockUseCase", "toggleShopLockUseCase",
            "deleteUserUseCase"})
    UserService userService(UserRepositoryPort userRepositoryPort, CachePort cachePort,
                            OrderActivityPort orderActivityPort, OutboxEventPort outboxEventPort, Clock clock) {
        return new UserService(userRepositoryPort, cachePort, orderActivityPort, outboxEventPort, clock);
    }

    @Bean
    EventHandler<OutboxEventDto> userDeletedOutboxHandler(AuthEventPublisherPort authEventPublisherPort) {
        return new UserDeletedOutboxHandler(authEventPublisherPort);
    }

    @Bean(name = {"authServiceInternal", "getPublicShopInfoUseCase", "internalGetUserInfoUseCase"})
    AuthServiceInternal authServiceInternal(
            TokenServicePort tokenServicePort,
            UserRepositoryPort userRepositoryPort
    ) {
        return new AuthServiceInternal(tokenServicePort, userRepositoryPort);
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

    @Bean
    ChangePasswordUseCase changePasswordUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

    @Bean
    ExchangeOAuthLoginCodeUseCase exchangeOAuthLoginCodeUseCase(AuthApplicationService authApplicationService) {
        return authApplicationService;
    }

    @Bean
    OAuthLoginHandler googleOAuthLoginHandler(AuthApplicationService authApplicationService) {
        return new GoogleOAuthLoginHandler(authApplicationService);
    }

    @Bean
    OAuthLoginHandlerRegistry oauthLoginHandlerRegistry(List<OAuthLoginHandler> oauthLoginHandlers) {
        return new OAuthLoginHandlerRegistry(oauthLoginHandlers);
    }

    @Bean
    OAuthLoginService oauthLoginService(OAuthLoginHandlerRegistry oauthLoginHandlerRegistry,
            AuthApplicationService authApplicationService) {
        return new OAuthLoginService(oauthLoginHandlerRegistry, authApplicationService);
    }


    @Bean
    DeleteStoredFilesService deleteStoredFilesService(FileStoragePort fileStoragePort) {
        return new DeleteStoredFilesService(fileStoragePort);
    }

    @Bean
    DeleteStoredFilesUseCase deleteStoredFilesUseCase(DeleteStoredFilesService deleteStoredFilesService) {
        return deleteStoredFilesService;
    }

    @Bean
    EventHandler<OutboxEventDto> storedFilesDeleteOutboxHandler(PayloadMapperPort payloadMapperPort,
                                                                DeleteStoredFilesService deleteStoredFilesService) {
        return new StoredFilesDeleteOutboxHandler(payloadMapperPort, deleteStoredFilesService);
    }

    @Bean(name = {"shopProfileService", "uploadShopLogoUseCase", "uploadAvatarUseCase", "getMyProfileUseCase",
            "updateMyProfileUseCase", "updateMyShopProfileUseCase"})
    ShopProfileService shopProfileService(UserRepositoryPort userRepositoryPort, TokenServicePort tokenServicePort,
                                          FileStoragePort fileStoragePort, StoredFileRollbackPort storedFileRollbackPort,
                                          OutboxEventPort outboxEventPort, Clock clock,
                                          AuthServiceInternal authServiceInternal) {
        return new ShopProfileService(userRepositoryPort, tokenServicePort, fileStoragePort, storedFileRollbackPort,
                outboxEventPort, clock, authServiceInternal);
    }
}
