package com.example.order_service.order.application.strategy.outbox;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;
import com.example.order_service.cart.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.cart.application.strategy.outbox.OutboxEventHandler;
import com.example.order_service.order.application.dto.event.CheckoutFailedPayload;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutFailedOutboxHandler implements OutboxEventHandler {
    private final OrderEventPublisherPort publisher;

    @Override
    public String eventType() {
        return CheckoutFailedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        publisher.publish(event);
    }
}
