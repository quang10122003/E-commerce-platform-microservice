package com.example.producr_service.application.strategy.FlashSaleCampaign;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FlashSaleCampaignPublishedHandler implements FlashSaleCampaignStatuHandler {
    Clock clock;

    @Override
    public FlashSaleCampaignStatus eventType() {
        return FlashSaleCampaignStatus.PUBLISHED;
    }

    @Override
    public void handle(FlashSaleCampaign flashSaleCampaign) {
       
        LocalDateTime now = LocalDateTime.now(clock);
        if (!now.isBefore(flashSaleCampaign.getEndsAt())) {
            flashSaleCampaign.updateStatus(FlashSaleCampaignStatus.DONE);
        }
    }
}
