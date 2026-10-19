package com.example.notification.domain.error;

import com.example.common.error.ErrorCode;

public enum DomainChatError implements ErrorCode {
    CHAT_ROOM_INVALID("CHAT_ROOM_INVALID", "Khách hàng, shop và sản phẩm phải có ID hợp lệ", 400),
    CHAT_MESSAGE_INVALID("CHAT_MESSAGE_INVALID", "Tin nhắn phải có người gửi và từ 1 đến 4000 ký tự", 400),
    CHAT_ACCESS_DENIED("CHAT_ACCESS_DENIED", "Bạn không thuộc cuộc trò chuyện này", 403);

    private final String code;
    private final String message;
    private final int httpStatusCode;

    DomainChatError(String code, String message, int httpStatusCode) {
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
