package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.dto.saga.CheckoutInventoryRequestedPayload;

import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.CheckoutInventoryService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
// event check out order gửi qua để product check giá tồn kho sale v.v
public class CheckoutInventoryRequestedHandler implements EventHandler {
    private final MapJsonToObjPort json;
    private final CheckoutInventoryService service;

    @Override
    public String eventType() {
        return CheckoutInventoryRequestedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        service.validateAndDeductCheckoutStock(json.readValue(message, CheckoutInventoryRequestedPayload.class));
    }
}
