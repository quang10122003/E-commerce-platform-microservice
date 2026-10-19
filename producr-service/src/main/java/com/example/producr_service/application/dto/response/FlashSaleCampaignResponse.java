package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record FlashSaleCampaignResponse(
        Long id,
        String name,
        String description,
        OffsetDateTime registrationDeadline,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        FlashSaleCampaignStatus status,
        Long createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    // Thời gian local phải dùng múi giờ lưu trữ để offset không làm đổi thời điểm chương trình.
    public static FlashSaleCampaignResponse from(FlashSaleCampaignView campaign, ZoneId zoneId) {
        return new FlashSaleCampaignResponse(
                campaign.id(),
                campaign.name(),
                campaign.description(),
                campaign.registrationDeadline().atZone(zoneId).toOffsetDateTime(),
                campaign.startsAt().atZone(zoneId).toOffsetDateTime(),
                campaign.endsAt().atZone(zoneId).toOffsetDateTime(),
                campaign.status(),
                campaign.createdBy(),
                campaign.createdAt().atZone(zoneId).toOffsetDateTime(),
                campaign.updatedAt().atZone(zoneId).toOffsetDateTime()
        );
    }
}
