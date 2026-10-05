package com.example.order_service.cart.application.dto.repone;

import com.example.order_service.cart.domain.model.ProductStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartRepone(Long CartId, int totalItems, int totalQuantity, List<CartItemInfo> item) {

    @Builder
    // thông tin các thuộc tính có thể có của cartItem
    public record VariantOptions(String name, String value) {

    }

    @Builder
    // thông tin của sản phẩm trong giỏ hàng
    public record CartItemInfo(Long cartItemId, Long productId, Long productVariantId,
                               Long shopId, String shopName, String productName,
                               String imageUrl, boolean hasOption,
                               List<VariantOptions> variantOptions, BigDecimal price, int quantity, int stockQuantity,
                               ProductStatus status) {

    }
}
