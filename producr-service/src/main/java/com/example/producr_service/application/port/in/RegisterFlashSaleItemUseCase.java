package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.RegisterFlashSaleItemRequest;

public interface RegisterFlashSaleItemUseCase {
    void register(Long campaignId, RegisterFlashSaleItemRequest request);
}
