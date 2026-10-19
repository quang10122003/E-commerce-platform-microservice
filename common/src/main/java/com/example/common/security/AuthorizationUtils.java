package com.example.common.security;


import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;

public final class AuthorizationUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    private AuthorizationUtils() {
    }

    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }

        String headerValue = authorizationHeader.trim();
        if (!headerValue.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }

        String token = headerValue.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new BusinessException(AuthorizationError.UNAUTHENTICATED);
        }

        return token;
    }
}
