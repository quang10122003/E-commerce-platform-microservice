package com.example.notification.application.dto;

import java.time.Instant;
import java.util.List;

public record WsTicketIdentity(Long userId, List<String> roles, Instant sessionExpiresAt) {
    public WsTicketIdentity {
        roles = List.copyOf(roles);
    }
}
