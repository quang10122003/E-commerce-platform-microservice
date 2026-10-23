package com.example.order_service.cart.application.strategy.outbox;

import com.example.order_service.cart.application.dto.outbox.CartItemAddedOutboxPayload;
import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.port.out.kafka.OrderEventPublisherPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartItemAddedOutboxHandler implements OutboxEventHandler {
    private final OrderEventPublisherPort orderEventPublisherPort;

    @Override
    public String eventType() {
        return CartItemAddedOutboxPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        orderEventPublisherPort.publish(event);
    }
}
