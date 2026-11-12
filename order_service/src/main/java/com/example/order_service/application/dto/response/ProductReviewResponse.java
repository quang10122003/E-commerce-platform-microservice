package com.example.order_service.application.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record ProductReviewResponse(Long id, Long orderItemId, Long productId, int rating, String content,
                                    String variantAttributes, List<String> imageUrls, OffsetDateTime createdAt) {
}
