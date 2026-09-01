package com.example.auth_service.application.error;

import org.springframework.stereotype.Component;

import com.example.auth_service.domain.exception.AuthDomainException;

@Component
public class OrderDomainErrorMapper {
    // map AuthDomainException sang AuthError(aplication) để bắn exception tầng aplication
    static final AuthError mapDomainErrorToApplicationErrorCode(AuthDomainException ex) {
        return switch (ex.getAuthDomainError()) {
            case USER_NOT_FOUND -> AuthError.USER_NOT_FOUND;
        };
    }
}
