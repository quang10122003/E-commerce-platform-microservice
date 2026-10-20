package com.example.producr_service.adapter.out.persistence.projection;

import java.math.BigDecimal;
import com.example.producr_service.domain.model.ProductStatus;

public interface VariantPriceProjection {
    Long getVariantId();
    BigDecimal getPrice();
    int getStockQuantity();
    ProductStatus getProductStatus();
}

