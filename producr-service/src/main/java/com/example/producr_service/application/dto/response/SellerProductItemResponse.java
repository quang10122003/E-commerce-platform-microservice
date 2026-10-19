package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SellerProductItemResponse(
        Long id,
        String name,
        String imageUrl,
        LocalDateTime createdAt,
        Long categoryId,
        String categoryName,
        DisplayStatus status,
        List<Variant> variants
) {
    public enum DisplayStatus {
        ACTIVE,
        OUT_OF_STOCK,
        INACTIVE
    }

    public record Variant(
            Long id,
            String sku,
            BigDecimal price,
            int stockQuantity,
            List<VariantAttribute> attributes
    ) {}

    public record VariantAttribute(
            Long attributeId,
            String name,
            String value
    ) {}
}
