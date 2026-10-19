package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.FlashSaleCampaignFilter;
import com.example.producr_service.application.dto.response.FlashSaleCampaignResponse;

public interface GetFlashSaleCampaignsUseCase {

    PageResponse<FlashSaleCampaignResponse> getCampaigns(FlashSaleCampaignFilter filter);
}
