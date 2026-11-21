package com.example.auth_service.application.strategy.outbox;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.DTO.event.ShopLockChangedEventDto;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;
import com.example.auth_service.application.strategy.EventHandler;

public class ShopLockChangedOutboxHandler implements EventHandler<OutboxEventDto> {

    private final AuthEventPublisherPort authEventPublisherPort;

    public ShopLockChangedOutboxHandler(AuthEventPublisherPort authEventPublisherPort) {
        this.authEventPublisherPort = authEventPublisherPort;
    }

    @Override
    public String eventType() {
        return ShopLockChangedEventDto.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        authEventPublisherPort.publish(event);
    }
}
