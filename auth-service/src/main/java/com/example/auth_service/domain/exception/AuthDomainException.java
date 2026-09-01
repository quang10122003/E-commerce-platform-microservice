package com.example.auth_service.domain.exception;

import com.example.auth_service.domain.error.AuthDomainError;

import lombok.Getter;

@Getter
public class AuthDomainException extends RuntimeException {

    private final AuthDomainError authDomainError;

    public AuthDomainException(AuthDomainError authDomainError) {
        super(authDomainError.getMessage());
        this.authDomainError = authDomainError;
    }
}