package com.example.order_service.cart.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class OutboxEvent {
    public static final int DEFAULT_MAX_RETRY = 5;
    public static final long INITIAL_RETRY_DELAY_SECONDS = 5;
    public static final long MAX_RETRY_DELAY_SECONDS = 300;
    public static final int MAX_RETRY_SHIFT = 5;

    private final UUID eventId;
    private final String aggregateType;
    private final String aggregateId;
    private final String eventType;
    private final String payload;
    private OutboxStatus status;
    private int retryCount;
    private final int maxRetry;
    private final LocalDateTime createdAt;
    private LocalDateTime nextAttemptAt;
    private LocalDateTime publishedAt;
    private String errorMessage;

    public OutboxEvent(UUID eventId, String aggregateType, String aggregateId, String eventType,
                       String payload, OutboxStatus status, int retryCount, int maxRetry,
                       LocalDateTime createdAt, LocalDateTime nextAttemptAt,
                       LocalDateTime publishedAt, String errorMessage) {
        this.eventId = Objects.requireNonNull(eventId);
        this.aggregateType = Objects.requireNonNull(aggregateType);
        this.aggregateId = Objects.requireNonNull(aggregateId);
        this.eventType = Objects.requireNonNull(eventType);
        this.payload = Objects.requireNonNull(payload);
        this.status = Objects.requireNonNull(status);
        if (retryCount < 0 || maxRetry < 1) {
            throw new IllegalArgumentException("Số lần retry outbox không hợp lệ");
        }
        this.retryCount = retryCount;
        this.maxRetry = maxRetry;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt);
        this.publishedAt = publishedAt;
        this.errorMessage = errorMessage;
    }

    public void markPublished(LocalDateTime now) {
        if (status != OutboxStatus.PENDING) {
            return;
        }
        status = OutboxStatus.PUBLISHED;
        publishedAt = now;
        errorMessage = null;
    }

    public void markFailed(LocalDateTime now, String error) {
        if (status != OutboxStatus.PENDING) {
            return;
        }
        retryCount++;
        errorMessage = error;
        if (retryCount >= maxRetry) {
            status = OutboxStatus.FAILED;
            return;
        }
        long delaySeconds = Math.min(MAX_RETRY_DELAY_SECONDS,
                INITIAL_RETRY_DELAY_SECONDS * (1L << Math.min(retryCount - 1, MAX_RETRY_SHIFT)));
        nextAttemptAt = now.plusSeconds(delaySeconds);
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
