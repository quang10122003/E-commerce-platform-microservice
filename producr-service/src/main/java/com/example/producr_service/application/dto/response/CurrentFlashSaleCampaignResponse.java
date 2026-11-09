package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.FlashSaleItemStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public record CurrentFlashSaleCampaignResponse(
        Long id, String name, String description, OffsetDateTime startsAt, OffsetDateTime endsAt,
        List<Item> items) {
    public CurrentFlashSaleCampaignResponse {
        items = List.copyOf(items);
    }

    public static CurrentFlashSaleCampaignResponse from(
            FlashSaleCampaignView campaign, List<Item> items, ZoneId zoneId) {
        return new CurrentFlashSaleCampaignResponse(campaign.id(), campaign.name(), campaign.description(),
                campaign.startsAt().atZone(zoneId).toOffsetDateTime(),
                campaign.endsAt().atZone(zoneId).toOffsetDateTime(), items);
    }

    public record Item(Long id, String productName, String variant_attributes, BigDecimal discountPercent,
                       int registeredQuantity, long sold_quality,
                       long remainingQuantity, FlashSaleItemStatus status, boolean variantDeleted) {}
}
