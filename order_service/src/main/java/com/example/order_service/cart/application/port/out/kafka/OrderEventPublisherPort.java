package com.example.order_service.cart.application.port.out.kafka;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;

public interface OrderEventPublisherPort {
    void publish(OutboxEventDto event);
}
