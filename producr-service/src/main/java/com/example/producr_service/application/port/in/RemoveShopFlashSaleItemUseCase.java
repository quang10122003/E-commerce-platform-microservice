package com.example.producr_service.application.port.in;

public interface RemoveShopFlashSaleItemUseCase {
    void removeItem(Long campaignId, Long itemId);
}
