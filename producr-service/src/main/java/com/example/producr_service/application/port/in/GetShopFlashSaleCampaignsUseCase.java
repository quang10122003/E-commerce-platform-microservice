package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.ShopFlashSaleCampaignFilter;
import com.example.producr_service.application.dto.response.ShopFlashSaleCampaignResponse;

public interface GetShopFlashSaleCampaignsUseCase {
    // Đọc trang chương trình DRAFT cho Shop xem và chuẩn bị đăng ký.
    PageResponse<ShopFlashSaleCampaignResponse> getShopCampaigns(ShopFlashSaleCampaignFilter filter);
}
