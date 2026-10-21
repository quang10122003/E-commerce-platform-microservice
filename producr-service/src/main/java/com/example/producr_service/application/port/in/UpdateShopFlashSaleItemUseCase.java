package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.UpdateShopFlashSaleItemRequest;

public interface UpdateShopFlashSaleItemUseCase {
    void updateItem(Long campaignId, Long itemId, UpdateShopFlashSaleItemRequest request);
}
