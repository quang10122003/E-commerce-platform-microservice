package com.example.producr_service.adapter.out.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductSaleOfferProjection {
    Long getProductId();
    Long getVariantId();
    BigDecimal getPrice();
    int getStockQuantity();
    BigDecimal getDiscountPercent();
    int getRegisteredQuantity();
    long getSoldQuantity();
    LocalDateTime getEndsAt();
}

