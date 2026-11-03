package com.example.order_service.order.application.strategy.outbox;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.cart.application.strategy.outbox.OutboxEventHandler;
import com.example.order_service.order.application.dto.saga.CheckoutInventoryRequestedPayload;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventoryRequestedOutboxHandler implements OutboxEventHandler {
    private final OrderEventPublisherPort publisher;

    @Override
    public String eventType() {
        return CheckoutInventoryRequestedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        publisher.publish(event);
    }
}
