package com.example.auth_service.domain.error;

import com.example.common.error.ErrorCode;

public enum DomainAuthError implements ErrorCode {
    USER_LOCKED("USER_LOCKED", "User account is locked", 423);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    DomainAuthError(String code, String message, int httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}
