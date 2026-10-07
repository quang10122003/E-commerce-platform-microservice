package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.CreateFlashSaleCampaignRequest;

public interface CreateFlashSaleCampaignUseCase {

    // Tạo chương trình Flash Sale bản nháp cho Admin hiện tại.
    void create(CreateFlashSaleCampaignRequest request);
}
