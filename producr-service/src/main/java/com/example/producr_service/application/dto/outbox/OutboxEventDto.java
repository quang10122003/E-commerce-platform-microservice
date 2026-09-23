package com.example.producr_service.application.dto.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.producr_service.domain.model.OutboxStatus;

// DTO trao đổi dữ liệu outbox giữa application, adapter và strategy.
public record OutboxEventDto(
        UUID eventId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        OutboxStatus status,
        int retryCount,
        int maxRetry,
        LocalDateTime createdAt,
        LocalDateTime nextAttemptAt,
        LocalDateTime publishedAt,
        String errorMessage
) {

    // Khởi tạo event mới ở trạng thái chờ xử lý.
    public OutboxEventDto(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload
    ) {
        this(
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                OutboxStatus.PENDING,
                0,
                5,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }

}
