package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

// Scheduler chỉ ghi log chuyển trạng thái sau khi transaction đã commit.
public record FlashSaleCampaignStatusTransition(
        FlashSaleCampaignStatus previousStatus,
        FlashSaleCampaignStatus currentStatus) {
}
