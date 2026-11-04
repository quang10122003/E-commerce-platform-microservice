package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.dto.saga.CheckoutSucceededPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.CheckoutInventoryService;
import com.example.producr_service.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutSucceededHandler implements EventHandler<String> {
    private final MapJsonToObjPort json;
    private final CheckoutInventoryService service;

    @Override
    public String eventType() {
        return CheckoutSucceededPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        service.increaseCheckoutTotalSold(json.readValue(message, CheckoutSucceededPayload.class));
    }
}
