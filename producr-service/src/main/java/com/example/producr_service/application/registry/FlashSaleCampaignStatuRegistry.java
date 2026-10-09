package com.example.producr_service.application.registry;

import com.example.producr_service.application.strategy.FlashSaleCampaign.FlashSaleCampaignStatuHandler;
import com.example.producr_service.application.strategy.outbox.OutboxEventHandler;
import com.example.producr_service.domain.model.FlashSaleCampaignStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlashSaleCampaignStatuRegistry {
    private  final Map<FlashSaleCampaignStatus, FlashSaleCampaignStatuHandler> handlers;

    public FlashSaleCampaignStatuRegistry(List<FlashSaleCampaignStatuHandler> handlers) {
        this.handlers = handlers.stream().collect(Collectors.toMap(FlashSaleCampaignStatuHandler::eventType,flashSaleCampaignStatuHandler -> flashSaleCampaignStatuHandler,(first,second)->{
            throw new IllegalStateException(
                    "Trùng handler cho eventType=" + first.eventType());
        }));
    }
    public FlashSaleCampaignStatuHandler handle(FlashSaleCampaignStatus flashSaleCampaignStatus){
        return Optional.ofNullable(handlers.get(flashSaleCampaignStatus)).orElseThrow(()->
                new IllegalStateException(
                        "Chưa đăng ký handler cho eventType=" + flashSaleCampaignStatus)
        );
    }

}
