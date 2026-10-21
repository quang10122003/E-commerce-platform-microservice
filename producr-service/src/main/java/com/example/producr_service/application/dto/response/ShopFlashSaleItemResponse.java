package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleItemStatus;
import java.math.BigDecimal;
import java.util.List;

public record ShopFlashSaleItemResponse(
        Long itemId,
        Long variantId,
        String productName,
        List<VariantOption> options,
        int registeredQuantity,
        Integer stockQuantity,
        FlashSaleItemStatus status,
        BigDecimal discountPercent,
        BigDecimal salePrice,
        boolean variantDeleted
) {
    public ShopFlashSaleItemResponse {
        options = List.copyOf(options);
    }

    public record VariantOption(String name, String value) {}
}
