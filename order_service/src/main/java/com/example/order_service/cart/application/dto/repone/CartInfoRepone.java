package com.example.order_service.cart.application.dto.repone;

import com.example.order_service.cart.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record CartInfoRepone(
        Long productVariantId,
        Long productId,
        Long shopId,
        String shopName,
        String productName,
        String imageUrl,
        boolean hasOption,
        List<VariantOption> variantOptions,
        BigDecimal price,
        int stockQuantity,
        ProductStatus status
) {
    public record VariantOption(String name, String value) {
    }
}
