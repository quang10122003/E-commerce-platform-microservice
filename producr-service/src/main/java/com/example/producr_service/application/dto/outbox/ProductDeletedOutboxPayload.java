package com.example.producr_service.application.dto.outbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductDeletedOutboxPayload(UUID eventId, String eventType,Long productId, List<Long> variantIds,LocalDateTime occurredAt) {
    public static final String EVENT_TYPE = "PRODUCT_DELETED";
}