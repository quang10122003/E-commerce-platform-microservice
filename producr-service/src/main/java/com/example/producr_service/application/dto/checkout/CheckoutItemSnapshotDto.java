package com.example.producr_service.application.dto.checkout;

import java.math.BigDecimal;

public record CheckoutItemSnapshotDto(
        Long productId, Long variantId, Long shopId, String productName, String variantAttributes,
        String imageUrl, int quantity, BigDecimal originalUnitPrice, BigDecimal unitPrice,
        boolean sale, Long saleItemId) {
}
