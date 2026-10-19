package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductShopIdResponse;

public interface GetProductShopIdUseCase {

    ProductShopIdResponse getShopId(Long productId);
}
