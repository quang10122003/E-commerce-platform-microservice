// aplication erorr của auth_service
package com.example.auth_service.application.error;

import com.example.common.error.ErrorCode;

public enum AuthError implements ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", 404),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role not found", 404),
    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED", "The email has already been registered.", 409),
    USER_LOCKED("USER_LOCKED", "User account is locked", 423),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Email or password is incorrect", 401),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID","Invalid refresh token",401);
    private final String code;
    private final String message;
    private final int httpStatusCode;

    AuthError(String code, String message, int httpStatusCode) {
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
