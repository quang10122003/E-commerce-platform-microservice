package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.ShopProductFilter;
import com.example.producr_service.application.dto.response.ProductSearchResponse;

public interface GetShopProductsUseCase {
    PageResponse<ProductSearchResponse> getShopProducts(Long shopId, ShopProductFilter filter);
}
