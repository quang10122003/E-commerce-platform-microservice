package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.saga.CheckoutInventoryFailedPayload;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventoryFailedHandler implements EventHandler<String> {
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
