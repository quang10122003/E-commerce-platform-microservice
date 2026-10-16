package com.example.notification.application.dto.response;

import java.time.OffsetDateTime;

// Vé dùng một lần cho CONNECT, không chứa JWT hoặc thông tin đăng nhập.
public record WsTicketResponse(String ticket, OffsetDateTime expiresAt) {
}
