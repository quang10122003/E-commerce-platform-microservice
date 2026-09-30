package com.example.producr_service.application.port.out;

import com.example.producr_service.application.dto.response.ShopProductDetailResponse;
import com.example.producr_service.domain.model.Product;

// Cho service tạo response mà không phụ thuộc trực tiếp vào mapper ở adapter.
public interface ShopProductDetailMapperPort {
    // Chỉ đưa thông tin người bán cần chỉnh sửa vào response.
    ShopProductDetailResponse toShopProductDetailResponse(Product product);
}
