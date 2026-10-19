package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;

import java.util.List;
import java.util.UUID;

public interface ProcessProductOutboxUseCase {

    List<OutboxEventDto> findPending();

    // Không lấy được khóa thì không chạy handler; caller phải phân biệt với đã xử lý thành công.
    boolean processEvent(OutboxEventDto event);

    void markFailed(UUID eventId, String errorMessage);
}
