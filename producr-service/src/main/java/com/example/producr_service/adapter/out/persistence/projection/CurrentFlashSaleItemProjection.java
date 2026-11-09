package com.example.producr_service.adapter.out.persistence.projection;

import com.example.producr_service.domain.model.FlashSaleItemStatus;
import java.math.BigDecimal;

public interface CurrentFlashSaleItemProjection {
    Long getId();
    String getProductName();
    String getVariantAttributes();
    BigDecimal getDiscountPercent();
    int getRegisteredQuantity();
    long getSoldQuantity();
    FlashSaleItemStatus getStatus();
    boolean getVariantDeleted();
}
