package com.example.producr_service.application.dto.outbox;


// Mang định danh để worker đọc lại trạng thái mới nhất khi đồng bộ chỉ mục.
public record ProductUpdatedOutboxPayload(Long productId, String location) {
    public static final String EVENT_TYPE = "PRODUCT_UPDATED";
}
