package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductShopIdResponse;

public interface GetProductShopIdUseCase {

    // Lấy ID shop của sản phẩm, báo lỗi khi sản phẩm không tồn tại.
    ProductShopIdResponse getShopId(Long productId);
}
