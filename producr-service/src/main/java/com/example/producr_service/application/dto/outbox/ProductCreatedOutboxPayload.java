package com.example.producr_service.application.dto.outbox;

public record ProductCreatedOutboxPayload(Long productId,String location) {

    public static final String EVENT_TYPE = "PRODUCT_CREATED";
}
