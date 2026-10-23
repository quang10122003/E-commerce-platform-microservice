package com.example.order_service.cart.application.port.out.outbox;

import com.example.order_service.cart.application.dto.outbox.OutboxEventDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxPort {
    void save(OutboxEventDto event);
    List<OutboxEventDto> findPending();
    Optional<OutboxEventDto> lockPendingEvent(UUID eventId);
    void markPublished(UUID eventId);
    void markFailed(UUID eventId, String errorMessage);
}
