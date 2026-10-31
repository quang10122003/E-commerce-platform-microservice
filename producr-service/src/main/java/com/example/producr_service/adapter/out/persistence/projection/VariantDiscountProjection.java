package com.example.producr_service.adapter.out.persistence.projection;

import java.math.BigDecimal;

public interface VariantDiscountProjection {
    Long getVariantId();
    BigDecimal getDiscountPercent();
}
