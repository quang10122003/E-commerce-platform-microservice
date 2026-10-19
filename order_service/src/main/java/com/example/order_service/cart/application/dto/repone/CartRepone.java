package com.example.order_service.cart.application.dto.repone;

import com.example.order_service.cart.domain.model.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record CartRepone(Long CartId, int totalItems, int totalQuantity, List<CartItemInfo> item) {

    @Builder
    public record VariantOptions(String name, String value) {

    }

    @Builder
    public record CartItemInfo(Long cartItemId, Long productId, Long productVariantId,
                               Long shopId, String shopName, String productName,
                               String imageUrl, boolean hasOption,
                               List<VariantOptions> variantOptions, BigDecimal price, int quantity, int stockQuantity,
                               ProductStatus status, Sale sale) {

    }

    public record Sale(BigDecimal salePrice, OffsetDateTime endsAt) {
    }
}
