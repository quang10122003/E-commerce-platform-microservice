package com.example.order_service.order.application.dto.saga;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckoutInventoryRolledBackPayload(
        UUID eventId, String eventType, Long checkoutId, UUID rollbackRequestEventId,
        String reasonCode, String reason, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "CHECKOUT_INVENTORY_ROLLED_BACK";
}
