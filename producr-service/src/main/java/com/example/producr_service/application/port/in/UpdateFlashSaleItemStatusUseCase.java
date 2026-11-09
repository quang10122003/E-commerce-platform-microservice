package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.UpdateFlashSaleItemStatusRequest;

public interface UpdateFlashSaleItemStatusUseCase {
    void updateItemStatus(Long campaignId, Long itemId, UpdateFlashSaleItemStatusRequest request);
}
