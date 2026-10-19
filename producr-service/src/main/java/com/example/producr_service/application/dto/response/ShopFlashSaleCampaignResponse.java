package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record ShopFlashSaleCampaignResponse(
        Long id,
        String name,
        OffsetDateTime registrationDeadline,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        boolean registrationOpen
) {
    // Quyền đăng ký phải dùng cùng mốc now với bộ lọc để response không tự mâu thuẫn.
    public static ShopFlashSaleCampaignResponse from(FlashSaleCampaignView campaign, ZoneId zone, LocalDateTime now) {
        return new ShopFlashSaleCampaignResponse(campaign.id(), campaign.name(),
                campaign.registrationDeadline().atZone(zone).toOffsetDateTime(),
                campaign.startsAt().atZone(zone).toOffsetDateTime(),
                campaign.endsAt().atZone(zone).toOffsetDateTime(),
                campaign.status() == FlashSaleCampaignStatus.DRAFT && now.isBefore(campaign.registrationDeadline()));
    }
}
