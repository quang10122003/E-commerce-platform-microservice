package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.dto.saga.CheckoutInventoryRolledBackPayload;
import com.example.order_service.application.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventoryRolledBackHandler implements EventHandler<String> {
    private final ObjectMapperPort json;
    private final CheckoutService checkoutService;

    @Override
    public String eventType() {
        return CheckoutInventoryRolledBackPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        CheckoutInventoryRolledBackPayload event = json.readValue(message, CheckoutInventoryRolledBackPayload.class);
        checkoutService.processInventoryRolledBack(event);
    }
}
