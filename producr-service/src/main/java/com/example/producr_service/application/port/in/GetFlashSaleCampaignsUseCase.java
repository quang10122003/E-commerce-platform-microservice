package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.FlashSaleCampaignFilter;
import com.example.producr_service.application.dto.response.FlashSaleCampaignResponse;

public interface GetFlashSaleCampaignsUseCase {

    // Đọc danh sách chương trình phân trang cho Admin, số trang đầu tiên là 1.
    PageResponse<FlashSaleCampaignResponse> getCampaigns(FlashSaleCampaignFilter filter);
}
