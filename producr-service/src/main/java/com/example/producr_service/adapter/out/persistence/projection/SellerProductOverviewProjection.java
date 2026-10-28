package com.example.producr_service.adapter.out.persistence.projection;

import com.example.producr_service.domain.model.ProductStatus;

public interface SellerProductOverviewProjection {
    ProductStatus getStatus();
    long getTotalProducts();
    long getInStockProducts();
}
