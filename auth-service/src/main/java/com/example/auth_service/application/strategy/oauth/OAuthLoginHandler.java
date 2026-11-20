package com.example.auth_service.application.strategy.oauth;

import com.example.auth_service.application.DTO.command.OAuthLoginCommand;
import com.example.auth_service.domain.model.User;

public interface OAuthLoginHandler {
    String registrationId();

    User login(OAuthLoginCommand command);
}
