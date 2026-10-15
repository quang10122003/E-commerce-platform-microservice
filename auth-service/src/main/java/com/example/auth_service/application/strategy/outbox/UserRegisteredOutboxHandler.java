package com.example.auth_service.application.strategy.outbox;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.DTO.event.UserRegisteredEventDto;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;

// Phát hành event đăng ký người dùng lên topic auth.
public class UserRegisteredOutboxHandler implements OutboxEventHandler {

    private final AuthEventPublisherPort authEventPublisherPort;

    public UserRegisteredOutboxHandler(AuthEventPublisherPort authEventPublisherPort) {
        this.authEventPublisherPort = authEventPublisherPort;
    }

    @Override
    public String eventType() {
        return UserRegisteredEventDto.Event_Type;
    }

    @Override
    public void handle(OutboxEventDto event) {
        authEventPublisherPort.publish(event);
    }
}
