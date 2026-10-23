package com.example.order_service.cart.application.dto.outbox;

import com.example.order_service.cart.domain.model.OutboxEvent;
import com.example.order_service.cart.domain.model.OutboxStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutboxEventDto(
        UUID eventId, String aggregateType, String aggregateId, String eventType, Object payload,
        OutboxStatus status, int retryCount, int maxRetry, LocalDateTime createdAt,
        LocalDateTime nextAttemptAt, LocalDateTime publishedAt, String errorMessage
) {
    public OutboxEventDto(UUID eventId, String aggregateType, String aggregateId,
                          String eventType, Object payload, LocalDateTime now) {
        this(eventId, aggregateType, aggregateId, eventType, payload, OutboxStatus.PENDING,
                0, OutboxEvent.DEFAULT_MAX_RETRY, now, now, null, null);
    }
}
