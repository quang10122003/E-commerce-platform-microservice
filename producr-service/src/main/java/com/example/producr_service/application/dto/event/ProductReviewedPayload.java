package com.example.producr_service.application.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Order Service đã tính điểm trung bình mới gồm cả đánh giá vừa tạo.
public record ProductReviewedPayload(UUID eventId, String eventType, Long productId, BigDecimal ratingAverage,
                                     OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "PRODUCT_REVIEWED";
}
