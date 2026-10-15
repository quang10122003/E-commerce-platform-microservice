package com.example.producr_service.application.port.out.kafka;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;

public interface ProductEventPublisherPort {
    void publish(OutboxEventDto event);
}
