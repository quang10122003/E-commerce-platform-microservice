package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.util.List;

public record VariantCartView(
        Long variantId,
        Long productId,
        Long shopId,
        String productName,
        // ảnh của product
        String productImageUrl,
        ProductStatus status,
        BigDecimal price,
        int stockQuantity,
        List<OptionView> options,
        List<ImageView> images
) {
    public record OptionView(Long attributeId, Long valueId, String name, String value) {}
    // ảnh của Variant
    public record ImageView(Long id, String url, boolean primary) {}
}