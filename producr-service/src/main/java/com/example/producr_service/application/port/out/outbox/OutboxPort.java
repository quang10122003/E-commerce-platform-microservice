package com.example.producr_service.application.port.out.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxPort {

    // Event phải lưu cùng transaction nghiệp vụ để không mất sự kiện hoặc phát dữ liệu đã rollback.
    void save(OutboxEventDto event);

    List<OutboxEventDto> findPending();

    // Phải giữ khóa event trong lúc chạy handler để tránh nhiều instance xử lý đồng thời.
    Optional<OutboxEventDto> lockPendingEvent(UUID eventId);

    void markPublished(UUID eventId);

    void markFailed(UUID eventId, String errorMessage);
}
