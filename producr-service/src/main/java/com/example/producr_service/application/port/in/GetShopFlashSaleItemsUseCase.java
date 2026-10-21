package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ShopFlashSaleItemResponse;
import java.util.List;

public interface GetShopFlashSaleItemsUseCase {
    List<ShopFlashSaleItemResponse> getShopItems(Long campaignId, String keyword);
}
