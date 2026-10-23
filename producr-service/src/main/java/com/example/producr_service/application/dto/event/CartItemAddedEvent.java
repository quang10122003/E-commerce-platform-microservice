package com.example.producr_service.application.dto.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CartItemAddedEvent(
        UUID eventId, String eventType, Long userId, Long productVariantId,
        Integer quantity, OffsetDateTime occurredAt
) {
    public static final String EVENT_TYPE = "CART_ITEM_ADDED";

    public CartItemAddedEvent {
        if (eventId == null || !EVENT_TYPE.equals(eventType) || userId == null || userId <= 0
                || productVariantId == null || productVariantId <= 0
                || quantity == null || quantity <= 0 || occurredAt == null) {
            throw new IllegalArgumentException("Event thêm giỏ thiếu hoặc sai dữ liệu");
        }
    }
}
