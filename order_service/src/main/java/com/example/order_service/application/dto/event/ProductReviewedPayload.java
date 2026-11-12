package com.example.order_service.application.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Product Service tăng số đánh giá thêm 1 và ghi đè điểm trung bình đã tính tại Order Service.
public record ProductReviewedPayload(UUID eventId, String eventType, Long productId, BigDecimal ratingAverage,
                                     OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "PRODUCT_REVIEWED";
}
