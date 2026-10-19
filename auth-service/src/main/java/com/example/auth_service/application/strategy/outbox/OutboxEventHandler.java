package com.example.auth_service.application.strategy.outbox;

import com.example.auth_service.application.DTO.OutboxEventDto;

public interface OutboxEventHandler {

    String eventType();

    void handle(OutboxEventDto event);
}
