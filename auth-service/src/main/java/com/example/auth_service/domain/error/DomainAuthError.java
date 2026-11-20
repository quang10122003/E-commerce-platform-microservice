package com.example.auth_service.domain.error;

import com.example.common.error.ErrorCode;

public enum DomainAuthError implements ErrorCode {
    ADMIN_LOCK_FORBIDDEN("ADMIN_LOCK_FORBIDDEN", "Không được khóa tài khoản có quyền ADMIN", 403),
    ADMIN_DELETE_FORBIDDEN("ADMIN_DELETE_FORBIDDEN", "Không được xóa tài khoản có quyền ADMIN", 403),
    USER_DELETED("USER_DELETED", "Tài khoản đã bị xóa", 409),
    SHOP_LOCKED("SHOP_LOCKED", "Shop đang bị khóa", 403),
    SHOP_NOT_FOUND("SHOP_NOT_FOUND", "Không tìm thấy shop", 404),
    USER_LOCKED("USER_LOCKED", "User account is locked", 403),
    PASSWORD_NOT_SUPPORTED("PASSWORD_NOT_SUPPORTED", "Tài khoản đăng nhập bằng Google không dùng mật khẩu", 400),
    INVALID_FULL_NAME("INVALID_FULL_NAME", "Họ và tên không được để trống và tối đa 30 ký tự", 400),
    INVALID_SHOP_NAME("INVALID_SHOP_NAME", "Tên shop không được để trống và tối đa 100 ký tự", 400),
    INVALID_SHOP_DESCRIPTION("INVALID_SHOP_DESCRIPTION", "Mô tả shop tối đa 2000 ký tự", 400);

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
