package com.example.auth_service.application.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.auth_service.domain.model.OutboxEvent;

// DTO chứa dữ liệu outbox  chuyển qua apdaoter
public record OutboxEventDto(
        UUID eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        Object payload,
        OutboxEvent.Status status,
        int retryCount,
        int maxRetry,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        String errorMessage) {
}
