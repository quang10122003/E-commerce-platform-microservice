package com.example.auth_service.application.port.out;

import com.example.auth_service.application.DTO.OutboxEventDto;

// Cổng phát hành event auth ra hệ thống message broker.
public interface AuthEventPublisherPort {

    // Phát hành payload của outbox event.
    void publish(OutboxEventDto event);
}
