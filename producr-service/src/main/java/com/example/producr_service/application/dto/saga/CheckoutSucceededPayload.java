package com.example.producr_service.application.dto.saga;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CheckoutSucceededPayload(
        UUID eventId, String eventType, Long checkoutId, Long userId, String checkoutStatus,
        String paymentMethod, List<Long> orderIds, BigDecimal totalAmount, OffsetDateTime occurredAt,
        String buyerEmail, String buyerFullName, String urlPayment,
        UUID inventorySucceededEventId) {
    public static final String EVENT_TYPE = "CHECKOUT_SUCCEEDED";
}
