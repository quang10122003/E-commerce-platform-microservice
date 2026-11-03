package com.example.producr_service.application.dto.saga;

import com.example.producr_service.application.dto.checkout.CheckoutItemSnapshotDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// event báo validation và trừ tồn kho thành công
public record CheckoutInventorySucceededPayload(
        UUID eventId, String eventType, Long checkoutId, UUID requestEventId,
        List<CheckoutItemSnapshotDto> items, BigDecimal totalAmount, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "CHECKOUT_INVENTORY_SUCCEEDED";

    public CheckoutInventorySucceededPayload {
        items = items == null ? null : List.copyOf(items);
    }
}
