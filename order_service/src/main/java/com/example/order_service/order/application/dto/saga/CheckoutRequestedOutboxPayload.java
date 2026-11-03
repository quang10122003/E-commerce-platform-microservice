package com.example.order_service.order.application.dto.saga;

import com.example.order_service.order.application.dto.CheckoutItemDto;
import com.example.order_service.order.domain.model.CheckoutPaymentMethod;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record CheckoutRequestedOutboxPayload(
        UUID eventId, String eventType, Long checkoutId, Long userId,
        CheckoutPaymentMethod paymentMethod, List<CheckoutItemDto> items, OffsetDateTime occurredAt) {

    public static final String EVENT_TYPE = "CHECKOUT_REQUESTED";

    public CheckoutRequestedOutboxPayload {
        // Giữ payload bất biến nhưng để service quyết định hủy checkout khi items sai dữ liệu.
        items = items == null ? null : Collections.unmodifiableList(new ArrayList<>(items));
    }
}
