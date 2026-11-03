package com.example.order_service.order.application.strategy.eventKafka;

import com.example.order_service.order.application.dto.saga.CheckoutInventoryFailedPayload;

import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.order.application.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventoryFailedHandler implements EventHandler {
    private final ObjectMapperPort json;
    private final CheckoutService checkoutService;

    @Override
    public String eventType() {
        return CheckoutInventoryFailedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String message) {
        CheckoutInventoryFailedPayload event = json.readValue(message, CheckoutInventoryFailedPayload.class);
        checkoutService.processInventoryFailed(event);
    }
}
