package com.example.auth_service.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

// Mô hình domain của một outbox event trước khi được lưu xuống cơ sở dữ liệu.
public record OutboxEvent(
        UUID eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        Object payload,
        OutboxStatus status,
        int retryCount,
        int maxRetry,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        String errorMessage) {

    public OutboxEvent(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            Object payload,
            LocalDateTime now) {
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
                null,
                null);
    }

}
