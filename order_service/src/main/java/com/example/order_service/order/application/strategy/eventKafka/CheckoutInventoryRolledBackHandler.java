package com.example.order_service.order.application.strategy.eventKafka;

import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.order.application.dto.saga.CheckoutInventoryRolledBackPayload;
import com.example.order_service.order.application.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventoryRolledBackHandler implements EventHandler {
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
