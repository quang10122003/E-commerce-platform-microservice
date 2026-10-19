package com.example.auth_service.application.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.auth_service.domain.model.OutboxStatus;

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
        LocalDateTime publishedAt,
        String errorMessage) {

}
