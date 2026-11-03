package com.example.notification.application.dto.event;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record CheckoutFailedNotification(
        UUID eventId, String eventType, Long checkoutId, String checkoutStatus,
        String reasonCode, String reason, OffsetDateTime occurredAt
) {
    public static final String EVENT_TYPE = "CHECKOUT_FAILED";

    public CheckoutFailedNotification {
        Objects.requireNonNull(eventId);
        if (!EVENT_TYPE.equals(eventType) || checkoutId == null || checkoutId <= 0) {
            throw new IllegalArgumentException("Event checkout thất bại không hợp lệ");
        }
    }
}
