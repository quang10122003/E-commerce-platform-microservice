package com.example.notification.application.dto;

import java.time.Instant;

// Hạn vé chỉ giới hạn CONNECT, không thay thế hạn của phiên socket.
public record WsTicketData(WsTicketIdentity identity, Instant expiresAt) {
}
