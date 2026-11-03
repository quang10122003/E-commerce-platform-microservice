package com.example.notification.application.dto.response;

import com.example.notification.application.dto.event.CheckoutSucceededNotification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record CheckoutSucceededResponse(
        Long checkoutId, String checkoutStatus, String paymentMethod,
        List<Long> orderIds, BigDecimal totalAmount, OffsetDateTime occurredAt, String urlPayment
) {
    public CheckoutSucceededResponse {
        orderIds = List.copyOf(orderIds);
    }

    public static CheckoutSucceededResponse from(CheckoutSucceededNotification event) {
        return new CheckoutSucceededResponse(event.checkoutId(), event.checkoutStatus(), event.paymentMethod(),
                event.orderIds(), event.totalAmount(), event.occurredAt(), event.urlPayment());
    }
}
