package com.example.producr_service.application.dto.response;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ShopCompletedFlashSaleCampaignResponse(Long id, String name,
                                                     OffsetDateTime startsAt, OffsetDateTime endsAt) {
    public static ShopCompletedFlashSaleCampaignResponse from(CompletedFlashSaleCampaignView campaign, ZoneId zone) {
        return new ShopCompletedFlashSaleCampaignResponse(campaign.id(), campaign.name(),
                campaign.startsAt().atZone(zone).toOffsetDateTime(),
                campaign.endsAt().atZone(zone).toOffsetDateTime());
    }
}
