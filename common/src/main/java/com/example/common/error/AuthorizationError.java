package com.example.common.error;

// Khai báo các lỗi dùng chung khi kiểm tra Authorization header.
public enum AuthorizationError implements ErrorCode {
    AUTHORIZATION_HEADER_REQUIRED("AUTHORIZATION_HEADER_REQUIRED", "Authorization header is required", 401),
    AUTHORIZATION_HEADER_INVALID("AUTHORIZATION_HEADER_INVALID", "Authorization header must use Bearer scheme", 401),
    BEARER_TOKEN_INVALID("BEARER_TOKEN_INVALID", "Bearer token is invalid", 401),
    USER_LOCKED("USER_LOCKED", "User account is locked", 403),
    TOKEN_BLACKLISTED("TOKEN_BLACKLISTED", "Token has been revoked", 403),
    ACCESS_CONTROL_UNAVAILABLE("ACCESS_CONTROL_UNAVAILABLE", "Access control is temporarily unavailable", 503);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    // Khởi tạo thông tin phản hồi cho từng loại lỗi Authorization.
    AuthorizationError(String code, String message, int httpStatusCode) {
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
