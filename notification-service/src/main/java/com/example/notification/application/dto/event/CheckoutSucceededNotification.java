package com.example.notification.application.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CheckoutSucceededNotification(
        UUID eventId, String eventType, Long checkoutId, Long userId, String checkoutStatus,
        String paymentMethod, List<Long> orderIds, BigDecimal totalAmount, OffsetDateTime occurredAt,
        String buyerEmail, String buyerFullName, String urlPayment, UUID inventorySucceededEventId
) {
    public static final String EVENT_TYPE = "CHECKOUT_SUCCEEDED";

    public CheckoutSucceededNotification {
        Objects.requireNonNull(eventId);
        if (!EVENT_TYPE.equals(eventType) || checkoutId == null || checkoutId <= 0) {
            throw new IllegalArgumentException("Event checkout thành công không hợp lệ");
        }
        orderIds = List.copyOf(orderIds);
    }
}
