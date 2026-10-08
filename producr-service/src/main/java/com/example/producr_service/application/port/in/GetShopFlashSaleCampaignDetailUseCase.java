package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.ShopFlashSaleCampaignDetailResponse;

public interface GetShopFlashSaleCampaignDetailUseCase {
    ShopFlashSaleCampaignDetailResponse getShopCampaignDetail(Long campaignId);
}
