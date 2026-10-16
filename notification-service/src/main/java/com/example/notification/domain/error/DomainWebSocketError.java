package com.example.notification.domain.error;

import com.example.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainWebSocketError implements ErrorCode {
    WS_TICKET_UNAVAILABLE("WS_TICKET_UNAVAILABLE", "Dịch vụ cấp vé WebSocket tạm thời không khả dụng", 503);

    private final String code;
    private final String message;
    private final int httpStatusCode;
}
