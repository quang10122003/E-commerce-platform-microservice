package com.example.auth_service.domain.error;

import com.example.common.error.ErrorCode;

public enum DomainAuthError implements ErrorCode {
    ADMIN_LOCK_FORBIDDEN("ADMIN_LOCK_FORBIDDEN", "Không được khóa tài khoản có quyền ADMIN", 403),
    ADMIN_DELETE_FORBIDDEN("ADMIN_DELETE_FORBIDDEN", "Không được xóa tài khoản có quyền ADMIN", 403),
    USER_DELETED("USER_DELETED", "Tài khoản đã bị xóa", 409),
    SHOP_LOCKED("SHOP_LOCKED", "Shop đang bị khóa", 403),
    SHOP_NOT_FOUND("SHOP_NOT_FOUND", "Không tìm thấy shop", 404),
    USER_LOCKED("USER_LOCKED", "User account is locked", 403);

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
