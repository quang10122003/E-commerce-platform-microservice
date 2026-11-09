package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import com.example.producr_service.domain.model.FlashSaleItemStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public record AdminFlashSaleCampaignDetailResponse(
        Long id, String name, String description, OffsetDateTime registrationDeadline,
        OffsetDateTime startsAt, OffsetDateTime endsAt,
        FlashSaleCampaignStatus status, List<Item> items) {
    public AdminFlashSaleCampaignDetailResponse {
        items = List.copyOf(items);
    }

    public static AdminFlashSaleCampaignDetailResponse from(FlashSaleCampaignView campaign,
            List<CurrentFlashSaleCampaignResponse.Item> items, ZoneId zoneId) {
        return new AdminFlashSaleCampaignDetailResponse(campaign.id(), campaign.name(), campaign.description(),
                campaign.registrationDeadline().atZone(zoneId).toOffsetDateTime(),
                campaign.startsAt().atZone(zoneId).toOffsetDateTime(),
                campaign.endsAt().atZone(zoneId).toOffsetDateTime(), campaign.status(),
                items.stream().map(item -> new Item(item.id(), item.productName(), item.variant_attributes(),
                        item.discountPercent(), item.registeredQuantity(), item.sold_quality(),
                        item.remainingQuantity(), item.status(), item.variantDeleted())).toList());
    }

    public record Item(Long id, String productName, String variant_attributes, BigDecimal discountPercent,
                       int registeredQuantity, long sold_quality, long remainingQuantity,
                       FlashSaleItemStatus status, boolean variantDeleted) {}
}
