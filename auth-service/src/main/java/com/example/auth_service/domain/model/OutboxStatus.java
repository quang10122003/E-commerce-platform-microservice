package com.example.auth_service.domain.model;

// Trạng thái vòng đời của một sự kiện outbox trong domain.
public enum OutboxStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
