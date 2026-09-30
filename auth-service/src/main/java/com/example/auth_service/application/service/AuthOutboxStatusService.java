package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.application.port.in.ProcessAuthOutboxUseCase;
import com.example.auth_service.application.port.out.OutboxEventPort;
import com.example.auth_service.application.registry.OutboxEventHandlerRegistry;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public class AuthOutboxStatusService implements ProcessAuthOutboxUseCase {

    private final OutboxEventPort outboxEventPort;
    private final OutboxEventHandlerRegistry outboxEventHandlerRegistry;

    public AuthOutboxStatusService(OutboxEventPort outboxEventPort,
                                   OutboxEventHandlerRegistry outboxEventHandlerRegistry) {
        this.outboxEventPort = outboxEventPort;
        this.outboxEventHandlerRegistry = outboxEventHandlerRegistry;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventDto> findPending() {
        return outboxEventPort.findPending();
    }

    // Giữ khóa đến khi handler chạy xong và trạng thái event được ghi nhận.
    @Override
    @Transactional
    public boolean processEvent(OutboxEventDto candidate) {
        var lockedEvent = outboxEventPort.lockPendingEvent(candidate.eventId());
        if (lockedEvent.isEmpty()) {
            return false;
        }

        OutboxEventDto event = lockedEvent.get();
        outboxEventHandlerRegistry.handle(event.eventType()).handle(event);
        outboxEventPort.markPublished(event.eventId());
        return true;
    }

    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        outboxEventPort.markFailed(eventId, errorMessage);
    }
}
