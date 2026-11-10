package com.example.producr_service.application.dto.saga;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckoutInventoryRollbackRequestedPayload(
        UUID eventId, String eventType, Long checkoutId, UUID inventorySucceededEventId,
        UUID checkoutSucceededEventId, String reasonCode, String reason, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "CHECKOUT_INVENTORY_ROLLBACK_REQUESTED";
}
