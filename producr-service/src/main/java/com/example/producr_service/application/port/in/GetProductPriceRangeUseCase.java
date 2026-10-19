package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductPriceRangeResponse;

public interface GetProductPriceRangeUseCase {
    ProductPriceRangeResponse getProductPriceRange(Long productId);
}
