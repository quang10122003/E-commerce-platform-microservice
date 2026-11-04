package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.dto.saga.CheckoutInventorySucceededPayload;
import com.example.producr_service.application.port.out.kafka.ProductEventPublisherPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutInventorySucceededOutboxHandler implements EventHandler<OutboxEventDto> {
    private final ProductEventPublisherPort publisher;

    @Override
    public String eventType() {
        return CheckoutInventorySucceededPayload.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        publisher.publish(event);
    }
}
