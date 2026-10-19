package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ShopProductDetailResponse;

public interface GetShopProductDetailUseCase {
    ShopProductDetailResponse getShopProductDetail(Long productId);
}
