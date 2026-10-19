package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.CreateFlashSaleCampaignRequest;

public interface CreateFlashSaleCampaignUseCase {

    void create(CreateFlashSaleCampaignRequest request);
}
