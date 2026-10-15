package com.example.producr_service.application.dto.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductvariantDeleteOutboxPayload (UUID eventId,String eventType, Long productId, Long variantId, String location,LocalDateTime occurredAt){
    public static final String EVENT_TYPE = "PRODUCT_VARIANT_DELETE";
}