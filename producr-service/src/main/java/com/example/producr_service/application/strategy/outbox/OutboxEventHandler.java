package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;

public interface OutboxEventHandler {

    String eventType();

    void handle(OutboxEventDto event);
}
