package com.example.producr_service.application.dto.outbox;

public record ProductvariantDeleteOutboxPayload (Long productId, Long variantId, String location){
    public static final String EVENT_TYPE = "PRODUCT_VARIANT_DELETE";
}
