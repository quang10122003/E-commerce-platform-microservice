package com.example.notification.application.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEvent(UUID eventId,
                String eventType,
                Long userId,
                String email,
                String fullName,
                LocalDateTime occurredAt) {

}
