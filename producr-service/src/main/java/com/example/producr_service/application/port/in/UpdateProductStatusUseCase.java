package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.UpdateProductStatusRequest;

public interface UpdateProductStatusUseCase {
    void updateProductStatus(Long productId, UpdateProductStatusRequest request);
}
