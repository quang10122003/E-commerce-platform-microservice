package com.example.order_service.application.strategy.eventKafka;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.saga.CheckoutRequestedOutboxPayload;
import com.example.order_service.application.port.out.serialization.ObjectMapperPort;
import com.example.order_service.application.service.CheckoutProcessingService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutRequestedEventHandler implements EventHandler<String> {

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
