package com.example.order_service.application.port.out.kafka;

import com.example.order_service.application.dto.outbox.OutboxEventDto;

public interface OrderEventPublisherPort {
    void publish(OutboxEventDto event);
}
