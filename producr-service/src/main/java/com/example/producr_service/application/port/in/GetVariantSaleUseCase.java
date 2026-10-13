package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.VariantSaleResponse;

public interface GetVariantSaleUseCase {
    // Kiểm tra sale hiện áp dụng cho variant thuộc product.
    VariantSaleResponse getVariantSale(Long productId, Long variantId);
}
