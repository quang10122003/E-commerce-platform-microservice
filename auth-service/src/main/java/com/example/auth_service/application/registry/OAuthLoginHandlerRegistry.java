package com.example.auth_service.application.registry;

import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.strategy.oauth.OAuthLoginHandler;
import com.example.common.exception.BusinessException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OAuthLoginHandlerRegistry {

    private final Map<String, OAuthLoginHandler> handlers;

    public OAuthLoginHandlerRegistry(List<OAuthLoginHandler> oauthLoginHandlers) {
        this.handlers = oauthLoginHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        OAuthLoginHandler::registrationId,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Trùng handler cho registrationId=" + first.registrationId());
                        }
                ));
    }

    public OAuthLoginHandler handle(String registrationId) {
        return Optional.ofNullable(handlers.get(registrationId))
                .orElseThrow(() -> new BusinessException(AuthError.OAUTH_PROVIDER_NOT_SUPPORTED));
    }
}
