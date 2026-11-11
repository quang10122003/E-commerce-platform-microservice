package com.example.order_service.application.strategy.outbox;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.application.dto.event.OrderStatusChangedPayload;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderStatusChangedOutboxHandler implements EventHandler<OutboxEventDto> {
    private final OrderEventPublisherPort publisher;

    @Override
    public String eventType() {
        return OrderStatusChangedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        publisher.publish(event);
    }
}
