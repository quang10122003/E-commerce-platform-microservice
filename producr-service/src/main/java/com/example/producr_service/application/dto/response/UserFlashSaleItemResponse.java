package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;

// Dữ liệu card của một phân loại tham gia sale.
public record UserFlashSaleItemResponse(
        Long productId,
        Long variantId,
        Long shopId,
        String productName,
        String variantLabel,
        String imageUrl,
        BigDecimal originalPrice,
        BigDecimal discountPercent,
        BigDecimal salePrice,
        int registeredQuantity,
        long remainingQuantity,
        Availability availability
) {

    // Trạng thái hiển thị, không lưu vào database.
    public enum Availability {
        AVAILABLE,
        OUT_OF_STOCK,
        SOLD_OUT
    }
}
