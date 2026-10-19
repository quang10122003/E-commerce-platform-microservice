package com.example.notification.application.dto.event;

import java.time.OffsetDateTime;

public record ChatConversationChangedEvent(Long conversationId, OffsetDateTime occurredAt) {
}
