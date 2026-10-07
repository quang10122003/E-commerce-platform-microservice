package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;

// Thông tin chương trình sale với thời gian có offset để FE hiển thị đúng múi giờ.
public record FlashSaleCampaignResponse(
        Long id,
        String name,
        String description,
        OffsetDateTime registrationDeadline,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        // DRAFT nhận đăng ký; PUBLISHED công bố cho người mua; CANCELLED hủy; DONE hoàn thành.
        FlashSaleCampaignStatus status,
        Long createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    // Gắn múi giờ lưu trữ vào thời gian local, giữ đúng thời điểm chương trình.
    public static FlashSaleCampaignResponse from(FlashSaleCampaign campaign, ZoneId zoneId) {
        return new FlashSaleCampaignResponse(
                campaign.getId(),
                campaign.getName(),
                campaign.getDescription(),
                campaign.getRegistrationDeadline().atZone(zoneId).toOffsetDateTime(),
                campaign.getStartsAt().atZone(zoneId).toOffsetDateTime(),
                campaign.getEndsAt().atZone(zoneId).toOffsetDateTime(),
                campaign.getStatus(),
                campaign.getCreatedBy(),
                campaign.getCreatedAt().atZone(zoneId).toOffsetDateTime(),
                campaign.getUpdatedAt().atZone(zoneId).toOffsetDateTime()
        );
    }
}
