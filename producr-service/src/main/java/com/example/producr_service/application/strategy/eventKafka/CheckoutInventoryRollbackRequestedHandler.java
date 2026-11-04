package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.dto.saga.CheckoutInventoryRollbackRequestedPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.CheckoutInventoryService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventoryRollbackRequestedHandler implements EventHandler<String> {
    private final MapJsonToObjPort json;
    private final CheckoutInventoryService checkoutInventoryService;

    @Override
    public String eventType() {
        return CheckoutInventoryRollbackRequestedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        CheckoutInventoryRollbackRequestedPayload event = json.readValue(message, CheckoutInventoryRollbackRequestedPayload.class);
        checkoutInventoryService.rollbackCheckoutStock(event);
    }
}
