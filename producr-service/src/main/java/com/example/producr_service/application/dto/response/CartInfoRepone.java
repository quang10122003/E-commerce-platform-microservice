package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
        ProductStatus status,
        Sale sale
) {

    public record VariantOption(String name, String value) {
    }

    // sale null nghĩa là không còn ưu đãi áp dụng, không phải giá sale bằng 0.
    public record Sale(BigDecimal salePrice, OffsetDateTime endsAt) {
    }
}
