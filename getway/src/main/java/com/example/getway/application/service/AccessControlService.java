package com.example.getway.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.getway.application.model.TokenIdentity;
import com.example.getway.application.port.in.AuthorizeAccessUseCase;
import com.example.getway.application.port.out.AccessControlCachePort;
import com.example.getway.application.port.out.TokenVerificationPort;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccessControlService implements AuthorizeAccessUseCase {

    TokenVerificationPort tokenVerificationPort;
    AccessControlCachePort accessControlCachePort;

    @Override
    public void authorize(String token) {
        TokenIdentity identity = tokenVerificationPort.verify(token);

        if (accessControlCachePort.isUserLocked(identity.userId())) {
            throw new BusinessException(AuthorizationError.USER_LOCKED);
        }

        if (accessControlCachePort.isTokenBlacklisted(token)) {
            throw new BusinessException(AuthorizationError.TOKEN_BLACKLISTED);
        }
    }
}
