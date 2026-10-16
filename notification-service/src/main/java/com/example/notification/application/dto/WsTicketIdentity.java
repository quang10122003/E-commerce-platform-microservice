package com.example.notification.application.dto;

import java.time.Instant;
import java.util.List;

// Danh tính và hạn phiên socket được lấy từ access JWT đã xác thực trên HTTP.
public record WsTicketIdentity(Long userId, List<String> roles, Instant sessionExpiresAt) {
    public WsTicketIdentity {
        roles = List.copyOf(roles);
    }
}
