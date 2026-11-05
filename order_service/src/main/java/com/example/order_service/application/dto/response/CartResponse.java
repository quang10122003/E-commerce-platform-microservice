package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record CartResponse(Long CartId, int totalItems, int totalQuantity, List<CartItemInfo> item) {

    @Builder
    public record VariantOptions(String name, String value) {

    }

    @Builder
    public record CartItemInfo(Long cartItemId, Long productId, Long productVariantId,
                               Long shopId, String shopName, Boolean shopLock, String productName,
                               String imageUrl, boolean hasOption,
                               List<VariantOptions> variantOptions, BigDecimal price, int quantity, int stockQuantity,
                               ProductStatus status, Sale sale, int saleQuantity, int regularQuantity) {

    }

    public record Sale(BigDecimal salePrice, OffsetDateTime endsAt) {
    }
}
