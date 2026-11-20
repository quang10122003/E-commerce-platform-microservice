package com.example.auth_service.application.strategy.oauth;

import com.example.auth_service.application.DTO.command.OAuthLoginCommand;
import com.example.auth_service.application.service.AuthApplicationService;
import com.example.auth_service.domain.model.User;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class GoogleOAuthLoginHandler implements OAuthLoginHandler {
    public static final String REGISTRATION_ID = "google";

    AuthApplicationService authApplicationService;

    @Override
    public String registrationId() {
        return REGISTRATION_ID;
    }

    @Override
    public User login(OAuthLoginCommand command) {
        return authApplicationService.loginWithGoogle(command);
    }
}
