package com.example.order_service.adapter.out.persistence.projection;

import java.math.BigDecimal;

public interface ShopTopProductProjection {
    Long getProductId();
    String getProductName();
    String getImageUrl();
    long getQuantity();
    BigDecimal getRevenue();
}
