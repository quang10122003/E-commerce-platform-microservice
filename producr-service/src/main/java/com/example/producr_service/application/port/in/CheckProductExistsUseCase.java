package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductExistsResponse;

public interface CheckProductExistsUseCase {

    // Kiểm tra sản phẩm theo ID cho service gọi nội bộ.
    ProductExistsResponse checkExists(Long productId);
}
