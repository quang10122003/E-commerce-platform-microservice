package com.example.order_service.cart.application.dto.outbox;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CartItemAddedOutboxPayload(
        UUID eventId, String eventType, Long userId, Long productVariantId,
        int quantity, OffsetDateTime occurredAt
) {
    public static final String EVENT_TYPE = "CART_ITEM_ADDED";
}
