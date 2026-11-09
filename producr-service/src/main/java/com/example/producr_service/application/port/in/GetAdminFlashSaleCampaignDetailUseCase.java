package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.AdminFlashSaleCampaignDetailResponse;
import com.example.producr_service.domain.model.FlashSaleItemStatus;

public interface GetAdminFlashSaleCampaignDetailUseCase {
    AdminFlashSaleCampaignDetailResponse getCampaignDetail(Long campaignId, FlashSaleItemStatus status, String keyword);
}
