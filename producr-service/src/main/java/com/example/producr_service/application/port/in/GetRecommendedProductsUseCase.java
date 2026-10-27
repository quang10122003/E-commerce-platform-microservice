package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.common.response.PageQuery;
import com.example.producr_service.application.dto.response.ProductSearchResponse;

public interface GetRecommendedProductsUseCase {
    PageResponse<ProductSearchResponse> getRecommendedProducts(PageQuery pageQuery);
}
