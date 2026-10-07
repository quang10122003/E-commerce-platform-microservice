package com.example.producr_service.application.dto.request;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

import java.time.OffsetDateTime;

// Điều kiện lọc theo giờ có offset, trạng thái và phân trang từ FE.
public record FlashSaleCampaignFilter(
        int page,
        int size,
        OffsetDateTime from,
        OffsetDateTime to,
        FlashSaleCampaignStatus status
) {
}
