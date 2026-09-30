package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.OutboxEventDto;

import java.util.List;
import java.util.UUID;

public interface ProcessAuthOutboxUseCase {
    List<OutboxEventDto> findPending();

    boolean processEvent(OutboxEventDto candidate);

    void markFailed(UUID eventId, String errorMessage);
}
