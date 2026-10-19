package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.VariantSaleResponse;

public interface GetVariantSaleUseCase {
    VariantSaleResponse getVariantSale(Long productId, Long variantId);
}
