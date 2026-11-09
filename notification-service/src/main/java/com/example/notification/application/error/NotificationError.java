package com.example.notification.application.error;

import com.example.common.error.ErrorCode;

public enum NotificationError implements ErrorCode {
    BANNER_NOT_FOUND("BANNER_NOT_FOUND", "Banner không tồn tại", 404),
    BANNER_LIMIT_REACHED("BANNER_LIMIT_REACHED", "Chỉ được lưu tối đa 5 banner", 409),
    SSE_REPLAY_UNAVAILABLE("SSE_REPLAY_UNAVAILABLE", "Lịch sử SSE không còn đủ, cần tải lại dữ liệu", 409);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    NotificationError(String code, String message, int httpStatusCode) {
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
