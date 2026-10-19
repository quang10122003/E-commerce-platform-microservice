package com.example.notification.application.dto.response;

import com.example.notification.domain.model.ChatMessage;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ChatMessageResponse(Long id, Long conversationId, Long senderId, String content,
                                  boolean read, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public static ChatMessageResponse from(Long conversationId, ChatMessage message, ZoneId zoneId) {
        return new ChatMessageResponse(message.id(), conversationId, message.senderId(), message.content(),
                message.read(), message.createdAt() == null ? null : message.createdAt().atZone(zoneId).toOffsetDateTime(),
                message.updatedAt() == null ? null : message.updatedAt().atZone(zoneId).toOffsetDateTime());
    }
}
