package com.example.order_service.application.strategy.outbox;

import com.example.order_service.application.dto.event.ProductReviewedPayload;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.out.kafka.OrderEventPublisherPort;
import com.example.order_service.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductReviewedOutboxHandler implements EventHandler<OutboxEventDto> {
    private final OrderEventPublisherPort publisher;

    @Override
    public String eventType() {
        return ProductReviewedPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        publisher.publish(event);
    }
}
