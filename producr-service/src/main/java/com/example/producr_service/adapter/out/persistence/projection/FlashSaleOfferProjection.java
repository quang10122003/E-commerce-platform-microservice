package com.example.producr_service.adapter.out.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface FlashSaleOfferProjection {
    Long getVariantId();
    BigDecimal getDiscountPercent();
    int getRegisteredQuantity();
    long getSoldQuantity();
    LocalDateTime getEndsAt();
}

