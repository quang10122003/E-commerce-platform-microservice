package com.example.auth_service.application.strategy.outbox;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.port.out.AuthEventPublisherPort;
import com.example.auth_service.domain.until.EventType;

// Phát hành event đăng ký người dùng lên topic auth.
public class UserRegisteredOutboxHandler implements OutboxEventHandler {

    private final AuthEventPublisherPort authEventPublisherPort;

    public UserRegisteredOutboxHandler(AuthEventPublisherPort authEventPublisherPort) {
        this.authEventPublisherPort = authEventPublisherPort;
    }

    @Override
    public String eventType() {
        return EventType.USER_REGISTERED.getValue();
    }

    @Override
    public void handle(OutboxEventDto event) {
        authEventPublisherPort.publish(event);
    }
}
