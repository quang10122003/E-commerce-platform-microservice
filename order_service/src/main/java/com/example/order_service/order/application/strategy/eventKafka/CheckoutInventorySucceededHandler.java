package com.example.order_service.order.application.strategy.eventKafka;

import com.example.order_service.order.application.dto.saga.CheckoutInventorySucceededPayload;

import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.order.application.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventorySucceededHandler implements EventHandler {
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
