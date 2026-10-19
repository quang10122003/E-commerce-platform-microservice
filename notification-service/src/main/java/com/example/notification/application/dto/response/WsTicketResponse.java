package com.example.notification.application.dto.response;

import java.time.OffsetDateTime;

// Vé CONNECT chỉ dùng một lần, không được chứa JWT hoặc thông tin đăng nhập.
public record WsTicketResponse(String ticket, OffsetDateTime expiresAt) {
}
