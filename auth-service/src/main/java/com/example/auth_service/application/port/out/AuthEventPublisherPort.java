package com.example.auth_service.application.port.out;

import com.example.auth_service.application.DTO.OutboxEventDto;

public interface AuthEventPublisherPort {

    void publish(OutboxEventDto event);
}
