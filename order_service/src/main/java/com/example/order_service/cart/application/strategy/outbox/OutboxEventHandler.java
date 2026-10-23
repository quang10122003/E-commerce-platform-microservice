package com.example.order_service.cart.application.strategy.outbox;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;

public interface OutboxEventHandler {

    String eventType();

    void handle(OutboxEventDto event);
}
