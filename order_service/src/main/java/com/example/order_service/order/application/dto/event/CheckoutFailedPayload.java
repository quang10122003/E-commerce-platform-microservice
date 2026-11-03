package com.example.order_service.order.application.dto.event;

import com.example.order_service.order.domain.model.CheckoutStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CheckoutFailedPayload(
        UUID eventId, String eventType, Long checkoutId, CheckoutStatus checkoutStatus,
        String reasonCode, String reason, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "CHECKOUT_FAILED";
}
