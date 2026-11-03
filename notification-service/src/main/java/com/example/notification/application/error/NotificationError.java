package com.example.notification.application.error;

import com.example.common.error.ErrorCode;

public enum NotificationError implements ErrorCode {
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
