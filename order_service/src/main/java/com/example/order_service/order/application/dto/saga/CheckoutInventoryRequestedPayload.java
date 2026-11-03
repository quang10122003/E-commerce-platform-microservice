package com.example.order_service.order.application.dto.saga;

import com.example.order_service.order.application.dto.CheckoutItemDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// event gửi product service để xác định tôn kho gía , sale v,v
public record CheckoutInventoryRequestedPayload(
        UUID eventId, String eventType, Long checkoutId, BigDecimal totalAmount,
        List<CheckoutItemDto> items, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "CHECKOUT_INVENTORY_REQUESTED";

    public CheckoutInventoryRequestedPayload {
        items = items == null ? null : List.copyOf(items);
    }
}
