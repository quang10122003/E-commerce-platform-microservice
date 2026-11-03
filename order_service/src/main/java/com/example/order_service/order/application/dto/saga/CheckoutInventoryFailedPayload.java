package com.example.order_service.order.application.dto.saga;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckoutInventoryFailedPayload(
        UUID eventId, String eventType, Long checkoutId, UUID requestEventId,
        String reasonCode, String reason, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "CHECKOUT_INVENTORY_FAILED";
}
