package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.saga.CheckoutInventorySucceededPayload;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventorySucceededHandler implements EventHandler<String> {
    private final ObjectMapperPort json;
    private final CheckoutService checkoutService;

    @Override
    public String eventType() {
        return CheckoutInventorySucceededPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        CheckoutInventorySucceededPayload event = json.readValue(message, CheckoutInventorySucceededPayload.class);
        checkoutService.processInventorySucceeded(event);
    }
}
