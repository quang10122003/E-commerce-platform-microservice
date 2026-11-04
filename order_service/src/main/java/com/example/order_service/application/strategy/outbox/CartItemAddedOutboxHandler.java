package com.example.order_service.application.strategy.outbox;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.outbox.CartItemAddedOutboxPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.out.kafka.OrderEventPublisherPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartItemAddedOutboxHandler implements EventHandler<OutboxEventDto> {
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
