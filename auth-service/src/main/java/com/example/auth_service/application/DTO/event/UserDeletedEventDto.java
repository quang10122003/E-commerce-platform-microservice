package com.example.auth_service.application.DTO.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDeletedEventDto(UUID eventId, String eventType, Long userId, LocalDateTime occurredAt) {
    public static final String EVENT_TYPE = "USER_DELETED";
}
