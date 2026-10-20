package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductSaleOfferView(
        Long productId,
        Long variantId,
        BigDecimal price,
        int stockQuantity,
        BigDecimal discountPercent,
        int registeredQuantity,
        long soldQuantity,
        LocalDateTime endsAt
) {
    public FlashSaleOfferView toOffer() {
        return new FlashSaleOfferView(variantId, discountPercent, registeredQuantity, soldQuantity, endsAt);
    }
}
