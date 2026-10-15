package com.example.order_service.cart.application.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductDeleteEvent(UUID eventId, String eventType, Long productId, List<Long> variantIds, LocalDateTime occurredAt) {
    public static final String EVENT_TYPE = "PRODUCT_DELETED";
}
