package com.example.producr_service.adapter.out.persistence.projection;

import java.math.BigDecimal;

public interface VariantUnitPriceProjection {
    Long getVariantId();
    BigDecimal getPrice();
}
