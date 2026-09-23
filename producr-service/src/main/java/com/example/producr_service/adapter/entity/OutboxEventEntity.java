package com.example.producr_service.adapter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    String eventId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    String aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "json")
    String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    Status status = Status.PENDING;

    @Column(name = "retry_count", nullable = false)
    int retryCount;

    @Column(name = "max_retry", nullable = false)
    int maxRetry = 5;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    LocalDateTime nextAttemptAt;

    @Column(name = "published_at")
    LocalDateTime publishedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;

    public enum Status {
        PENDING,
        PUBLISHED,
        FAILED
    }
}
