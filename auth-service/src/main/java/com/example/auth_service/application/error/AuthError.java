package com.example.auth_service.application.error;

import com.example.common.error.ErrorCode;

public enum AuthError implements ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", 404),
    INVALID_IMAGE_FILE("INVALID_IMAGE_FILE", "File ảnh không hợp lệ", 400),
    USER_HAS_ACTIVE_ORDERS("USER_HAS_ACTIVE_ORDERS", "Tài khoản còn đơn hàng hoặc thanh toán đang xử lý nên chưa thể xóa", 409),
    ORDER_SERVICE_UNAVAILABLE("ORDER_SERVICE_UNAVAILABLE", "Tạm thời không kiểm tra được đơn hàng của tài khoản", 503),
    SHOP_NOT_FOUND("SHOP_NOT_FOUND", "Không tìm thấy shop", 404),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND", "Role not found", 404),
    ADDRESS_NOT_FOUND("ADDRESS_NOT_FOUND", "Không tìm thấy địa chỉ", 404),
    EMAIL_ALREADY_REGISTERED("EMAIL_ALREADY_REGISTERED", "The email has already been registered.", 409),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Email or password is incorrect", 401),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID","Invalid refresh token",401),
    CURRENT_PASSWORD_INCORRECT("CURRENT_PASSWORD_INCORRECT", "Mật khẩu hiện tại không đúng", 400),
    PASSWORD_CONFIRM_MISMATCH("PASSWORD_CONFIRM_MISMATCH", "Mật khẩu nhập lại không khớp", 400),
    NEW_PASSWORD_SAME_AS_CURRENT("NEW_PASSWORD_SAME_AS_CURRENT", "Mật khẩu mới phải khác mật khẩu hiện tại", 400),
    OAUTH_PROVIDER_NOT_SUPPORTED("OAUTH_PROVIDER_NOT_SUPPORTED", "Phương thức đăng nhập chưa được hỗ trợ", 400),
    OAUTH_EMAIL_NOT_VERIFIED("OAUTH_EMAIL_NOT_VERIFIED", "Email của tài khoản Google chưa được xác minh", 401),
    OAUTH_LOGIN_FAILED("OAUTH_LOGIN_FAILED", "Đăng nhập Google thất bại", 401),
    OAUTH_LOGIN_CODE_INVALID("OAUTH_LOGIN_CODE_INVALID", "Mã đăng nhập không hợp lệ hoặc đã hết hạn", 401);
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
