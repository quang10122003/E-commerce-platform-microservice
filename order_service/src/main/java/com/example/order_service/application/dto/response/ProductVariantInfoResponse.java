package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ProductVariantInfoResponse(
        Long productVariantId,
        Long productId,
        Long shopId,
        boolean shopLocked,
        String shopName,
        String productName,
        String imageUrl,
        boolean hasOption,
        List<VariantOption> variantOptions,
        BigDecimal price,
        int stockQuantity,
        ProductStatus status,
        Sale sale
) {
    public record VariantOption(String name, String value) {
    }

    public record Sale(BigDecimal salePrice, OffsetDateTime endsAt, long remainingQuantity) {
    }
}
