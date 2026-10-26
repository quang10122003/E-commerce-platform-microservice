package com.example.producr_service.application.dto.outbox;

public record ProductEmbeddingSyncOutboxPayload(Long productId) {
    public static final String EVENT_TYPE = "PRODUCT_EMBEDDING_SYNC";
}
