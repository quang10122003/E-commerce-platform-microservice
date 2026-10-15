package com.example.order_service.cart.application.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductvariantDeleteEvent(UUID eventId, String eventType, Long productId, Long variantId, String location,
                                        LocalDateTime occurredAt) {
    public static final String EVENT_TYPE = "PRODUCT_VARIANT_DELETE";
}
