package com.example.order_service.application.strategy.outbox;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.application.dto.event.CheckoutFailedPayload;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutFailedOutboxHandler implements EventHandler<OutboxEventDto> {
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
