package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;

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

    // Trạng thái này chỉ phục vụ hiển thị, không phải trạng thái được lưu trong database.
    public enum Availability {
        AVAILABLE,
        OUT_OF_STOCK,
        SOLD_OUT
    }
}
