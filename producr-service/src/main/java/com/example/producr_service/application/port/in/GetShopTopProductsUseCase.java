package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductSearchResponse;

import java.util.List;

public interface GetShopTopProductsUseCase {
    List<ProductSearchResponse> getShopTopProducts(Long shopId);
}
