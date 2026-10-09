package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

// Kết quả đổi trạng thái để scheduler ghi log sau khi transaction commit.
public record FlashSaleCampaignStatusTransition(
        FlashSaleCampaignStatus previousStatus,
        FlashSaleCampaignStatus currentStatus) {
}
