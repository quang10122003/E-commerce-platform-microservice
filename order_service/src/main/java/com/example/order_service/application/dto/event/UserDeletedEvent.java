package com.example.order_service.application.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDeletedEvent(UUID eventId, String eventType, Long userId, LocalDateTime occurredAt) {
    public static final String EVENT_TYPE = "USER_DELETED";
}
