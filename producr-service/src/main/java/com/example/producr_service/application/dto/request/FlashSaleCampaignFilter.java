package com.example.producr_service.application.dto.request;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

import java.time.OffsetDateTime;

public record FlashSaleCampaignFilter(
        int page,
        int size,
        OffsetDateTime from,
        OffsetDateTime to,
        FlashSaleCampaignStatus status
) {
}
