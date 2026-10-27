package com.example.producr_service.adapter.out.persistence.projection;

import java.math.BigDecimal;

public interface ProductPreferenceProjection {

    Long getProductId();

    Long getCategoryId();

    Long getBrandId();

    BigDecimal getPrice();
}