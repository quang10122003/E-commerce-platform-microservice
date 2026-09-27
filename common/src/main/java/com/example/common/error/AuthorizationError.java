package com.example.common.error;

// Khai báo các lỗi xác thực và phân quyền dùng chung.
public enum AuthorizationError implements ErrorCode {
    UNAUTHENTICATED("UNAUTHENTICATED", "Authentication is required to access this resource", 401),
    ACCESS_DENIED("ACCESS_DENIED", "You do not have permission to access this resource", 403),
    USER_LOCKED("USER_LOCKED", "User account is locked", 403),
    TOKEN_BLACKLISTED("TOKEN_BLACKLISTED", "Token has been revoked", 403),
    ACCESS_CONTROL_UNAVAILABLE("ACCESS_CONTROL_UNAVAILABLE", "Access control is temporarily unavailable at the API Gateway.", 503);

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
