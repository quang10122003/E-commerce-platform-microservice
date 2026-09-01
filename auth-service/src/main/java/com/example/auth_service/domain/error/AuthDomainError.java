// Enum lỗi của auth service domain
package com.example.auth_service.domain.error;

public enum AuthDomainError {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found");

    private final String code;
    private final String message;

    AuthDomainError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
