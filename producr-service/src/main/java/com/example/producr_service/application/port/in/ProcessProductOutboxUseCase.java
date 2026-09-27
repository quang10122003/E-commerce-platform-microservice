package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;

import java.util.List;
import java.util.UUID;

// Cung cấp các thao tác xử lý event outbox cho scheduler.
public interface ProcessProductOutboxUseCase {

    // Lấy các event đã đến hạn xử lý.
    List<OutboxEventDto> findPending();

    // Chạy handler và ghi nhận event đã xử lý thành công.
    void processEvent(OutboxEventDto event);

    // Ghi nhận lỗi để event được thử lại theo lịch.
    void markFailed(UUID eventId, String errorMessage);
}
