// aplication erorr của auth_service
package com.example.auth_service.application.error;

import com.example.common.error.ErrorCode;

public enum AuthError implements ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "User not found"),
    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED", "The email has already been registered.");

    private final String code;
    private final String message;

    AuthError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
