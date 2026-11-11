package com.example.producr_service.application.strategy.eventKafka;

import com.example.producr_service.application.dto.saga.OrderInventoryRollbackRequestedPayload;
import com.example.producr_service.application.port.out.Json.MapJsonToObjPort;
import com.example.producr_service.application.service.CheckoutInventoryService;
import com.example.producr_service.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderInventoryRollbackRequestedHandler implements EventHandler<String> {
    private final MapJsonToObjPort json;
    private final CheckoutInventoryService checkoutInventoryService;

    @Override
    public String eventType() {
        return OrderInventoryRollbackRequestedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        OrderInventoryRollbackRequestedPayload event = json.readValue(message, OrderInventoryRollbackRequestedPayload.class);
        checkoutInventoryService.rollbackOrderStock(event);
    }
}
