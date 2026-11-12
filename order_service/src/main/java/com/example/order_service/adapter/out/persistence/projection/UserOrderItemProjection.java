package com.example.order_service.adapter.out.persistence.projection;

import java.math.BigDecimal;

public interface UserOrderItemProjection {
    Long getOrderId();
    Long getOrderItemId();
    Long getProductId();
    Long getVariantId();
    String getProductName();
    String getVariantAttributes();
    String getImageUrl();
    int getQuantity();
    BigDecimal getOriginalUnitPrice();
    BigDecimal getUnitPrice();
    BigDecimal getTotalAmount();
    boolean getReviewed();
}
