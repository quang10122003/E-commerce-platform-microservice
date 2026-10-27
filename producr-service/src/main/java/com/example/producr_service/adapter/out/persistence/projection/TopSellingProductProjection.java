package com.example.producr_service.adapter.out.persistence.projection;

import com.example.producr_service.domain.model.ProductStatus;

import java.math.BigDecimal;

public interface TopSellingProductProjection {
    Long getId();
    String getName();
    String getLocationSnapshot();
    Long getUserId();
    String getDescription();
    Long getCategoryId();
    String getCategoryName();
    Long getBrandId();
    String getBrandName();
    ProductStatus getStatus();
    String getImageUrl();
    BigDecimal getMaxPrice();
    long getTotalSold();
}
