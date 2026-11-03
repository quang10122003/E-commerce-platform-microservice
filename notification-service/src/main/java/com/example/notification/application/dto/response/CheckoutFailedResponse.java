package com.example.notification.application.dto.response;

import com.example.notification.application.dto.event.CheckoutFailedNotification;

import java.time.OffsetDateTime;

public record CheckoutFailedResponse(
        Long checkoutId, String checkoutStatus, String reasonCode, String reason, OffsetDateTime occurredAt
) {
    public static CheckoutFailedResponse from(CheckoutFailedNotification event) {
        return new CheckoutFailedResponse(event.checkoutId(), event.checkoutStatus(),
                event.reasonCode(), event.reason(), event.occurredAt());
    }
}
