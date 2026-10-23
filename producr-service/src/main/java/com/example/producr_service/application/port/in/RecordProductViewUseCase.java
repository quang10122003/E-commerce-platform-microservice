package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.RecordProductViewRequest;

public interface RecordProductViewUseCase {
    void recordProductView(Long productId, RecordProductViewRequest request);
}
