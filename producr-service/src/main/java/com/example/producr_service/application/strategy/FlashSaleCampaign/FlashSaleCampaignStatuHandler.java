package com.example.producr_service.application.strategy.FlashSaleCampaign;

import com.example.producr_service.domain.model.FlashSaleCampaign;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

public interface FlashSaleCampaignStatuHandler {

    FlashSaleCampaignStatus eventType();

    void handle(FlashSaleCampaign flashSaleCampaign);
}
