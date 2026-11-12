package com.example.order_service.adapter.out.persistence.projection;

import java.math.BigDecimal;

public interface TopShopProjection {
    Long getShopId();
    String getShopName();
    long getOrderCount();
    BigDecimal getRevenue();
}
