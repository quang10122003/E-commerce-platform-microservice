package com.example.order_service.order.application.strategy.outbox;

import com.example.order_service.order.application.dto.saga.CheckoutRequestedOutboxPayload;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.cart.application.strategy.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutRequestedOutboxHandler implements OutboxEventHandler {

    private final OrderEventPublisherPort orderEventPublisherPort;

    @Override
    public String eventType() {
        return CheckoutRequestedOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        orderEventPublisherPort.publish(event);
    }
}
