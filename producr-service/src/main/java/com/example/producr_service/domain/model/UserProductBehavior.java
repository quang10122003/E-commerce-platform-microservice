package com.example.producr_service.domain.model;

import java.time.Instant;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

public final class UserProductBehavior {
    public static final Duration RETENTION = Duration.ofDays(30);
    private final String eventId;
    private final Long userId;
    private final Long productId;
    private final BehaviorType eventType;
    private final Instant createdAt;

    public UserProductBehavior(String eventId, Long userId, Long productId,
                               BehaviorType eventType, Instant createdAt) {
        this.eventId = UUID.fromString(eventId).toString();
        this.userId = Objects.requireNonNull(userId);
        this.productId = Objects.requireNonNull(productId);
        this.eventType = Objects.requireNonNull(eventType);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public String getEventId() {
        return eventId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public BehaviorType getEventType() {
        return eventType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
