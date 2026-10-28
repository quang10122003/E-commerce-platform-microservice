package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.ShopCompletedFlashSaleCampaignFilter;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.ShopCompletedFlashSaleCampaignResponse;

public interface GetShopCompletedFlashSaleCampaignsUseCase {
    PageResponse<ShopCompletedFlashSaleCampaignResponse> getShopCompletedCampaigns(ShopCompletedFlashSaleCampaignFilter filter);
}
