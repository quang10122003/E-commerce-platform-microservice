package com.example.notification.application.error;

import com.example.common.error.ErrorCode;

public enum WebSocketError implements ErrorCode {
    WS_TICKET_UNAVAILABLE("WS_TICKET_UNAVAILABLE", "Dịch vụ cấp vé WebSocket tạm thời không khả dụng", 503);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    WebSocketError(String code, String message, int httpStatusCode) {
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
