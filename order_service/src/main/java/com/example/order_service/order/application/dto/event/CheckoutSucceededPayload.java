package com.example.order_service.order.application.dto.event;

import com.example.order_service.order.domain.model.CheckoutPaymentMethod;
import com.example.order_service.order.domain.model.CheckoutStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CheckoutSucceededPayload(
        UUID eventId, String eventType, Long checkoutId, Long userId, CheckoutStatus checkoutStatus,
        CheckoutPaymentMethod paymentMethod, List<Long> orderIds, BigDecimal totalAmount,
        OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "CHECKOUT_SUCCEEDED";

    public CheckoutSucceededPayload {
        orderIds = orderIds == null ? null : List.copyOf(orderIds);
    }
}
