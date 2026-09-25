package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.SellerProductFilter;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;

public interface GetSellerProductsUseCase {
    PageResponse<SellerProductItemResponse> getSellerProducts(SellerProductFilter filter);
}
