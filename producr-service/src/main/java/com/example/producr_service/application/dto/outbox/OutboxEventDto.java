package com.example.producr_service.application.dto.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.producr_service.domain.model.OutboxStatus;

public record OutboxEventDto(
        UUID eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        Object payload,
        OutboxStatus status,
        int retryCount,
        int maxRetry,
        LocalDateTime createdAt,
        LocalDateTime nextAttemptAt,
        LocalDateTime publishedAt,
        String errorMessage
) {

    public OutboxEventDto(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            Object payload,
            LocalDateTime now
    ) {
        this(
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxStatus.PENDING,
                0,
                5,
                now,
                now,
                null,
                null
        );
    }

}
