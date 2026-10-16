package com.example.notification.application.dto;

import java.time.Instant;

// Dữ liệu phía server phân biệt hạn dùng vé và hạn sử dụng phiên socket.
public record WsTicketData(WsTicketIdentity identity, Instant expiresAt) {
}
