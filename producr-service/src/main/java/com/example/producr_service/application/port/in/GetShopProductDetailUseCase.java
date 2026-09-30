package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ShopProductDetailResponse;

// Cung cấp thông tin sản phẩm cho người bán xem trước khi chỉnh sửa.
public interface GetShopProductDetailUseCase {
    ShopProductDetailResponse getShopProductDetail(Long productId);
}
