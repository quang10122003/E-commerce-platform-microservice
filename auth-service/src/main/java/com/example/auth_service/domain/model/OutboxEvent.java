package com.example.auth_service.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.auth_service.domain.until.EventType;

// Mô hình domain của một outbox event trước khi được lưu xuống cơ sở dữ liệu.
public record OutboxEvent(
        UUID eventId,
        String aggregateType,
        String aggregateId,
        EventType eventType,
        Object payload,
        Status status,
        int retryCount,
        int maxRetry,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        String errorMessage) {

    public OutboxEvent(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            EventType eventType,
            Object payload) {
        this(
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                Status.PENDING,
                0,
                5,
                LocalDateTime.now(),
                null,
                null);
    }

    public enum Status {
        PENDING,
        PUBLISHED,
        FAILED
    }
}
