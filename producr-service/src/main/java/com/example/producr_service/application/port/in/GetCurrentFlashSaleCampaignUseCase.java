package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.CurrentFlashSaleCampaignResponse;
import com.example.producr_service.domain.model.FlashSaleItemStatus;
import java.util.Optional;

public interface GetCurrentFlashSaleCampaignUseCase {
    Optional<CurrentFlashSaleCampaignResponse> getCurrentCampaign(FlashSaleItemStatus status, String keyword);
}
