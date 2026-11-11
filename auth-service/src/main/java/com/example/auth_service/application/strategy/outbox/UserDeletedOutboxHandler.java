package com.example.auth_service.application.strategy.outbox;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.DTO.event.UserDeletedEventDto;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;
import com.example.auth_service.application.strategy.EventHandler;

public class UserDeletedOutboxHandler implements EventHandler<OutboxEventDto> {

    private final AuthEventPublisherPort authEventPublisherPort;

    public UserDeletedOutboxHandler(AuthEventPublisherPort authEventPublisherPort) {
        this.authEventPublisherPort = authEventPublisherPort;
    }

    @Override
    public String eventType() {
        return UserDeletedEventDto.EVENT_TYPE;
    }

    @Override
    public void handle(OutboxEventDto event) {
        authEventPublisherPort.publish(event);
    }
}
