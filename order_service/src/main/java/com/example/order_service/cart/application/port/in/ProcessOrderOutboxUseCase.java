package com.example.order_service.cart.application.port.in;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;

import java.util.List;
import java.util.UUID;

public interface ProcessOrderOutboxUseCase {
    List<OutboxEventDto> findPending();
    boolean processEvent(OutboxEventDto event);
    void markFailed(UUID eventId, String errorMessage);
}
