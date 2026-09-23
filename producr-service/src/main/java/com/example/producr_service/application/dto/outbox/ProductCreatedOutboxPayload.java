package com.example.producr_service.application.dto.outbox;

// Payload outbox cho envet tạo sản phẩm
public record ProductCreatedOutboxPayload(Long productId) {

    public static final String EVENT_TYPE = "PRODUCT_CREATED";
}
