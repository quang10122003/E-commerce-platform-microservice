package com.example.order_service.order.application.strategy.eventKafka;

import com.example.order_service.order.application.dto.saga.CheckoutRequestedOutboxPayload;

import com.example.order_service.cart.application.port.out.objettMapper.ObjectMapperPort;
import com.example.order_service.order.application.service.CheckoutProcessingService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutRequestedEventHandler implements EventHandler {

    private final ObjectMapperPort objectMapperPort;
    private final CheckoutProcessingService checkoutProcessingService;

    @Override
    public String eventType() {
        return CheckoutRequestedOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(String event) {
        CheckoutRequestedOutboxPayload payload = objectMapperPort.readValue(
                event, CheckoutRequestedOutboxPayload.class);
        checkoutProcessingService.process(payload);
    }
}
