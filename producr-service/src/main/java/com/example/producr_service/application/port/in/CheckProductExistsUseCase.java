package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ProductExistsResponse;

public interface CheckProductExistsUseCase {

    ProductExistsResponse checkExists(Long productId);
}
