package com.example.auth_service.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.domain.model.OutboxEvent;

public interface OutboxEventPort {

    void save(OutboxEvent event);

    List<OutboxEventDto> findPending();

    // Khóa event còn chờ xử lý để chỉ một instance được publish.
    Optional<OutboxEventDto> lockPendingEvent(UUID eventId);

    void markPublished(UUID eventId);

    void markFailed(UUID eventId, String errorMessage);

}
