package com.example.order_service.application.service;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.dto.outbox.OutboxEventDto;
import com.example.order_service.application.port.in.ProcessOrderOutboxUseCase;
import com.example.order_service.application.port.out.outbox.OutboxPort;
import com.example.order_service.application.registry.OutboxEventHandlerRegistry;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderOutboxStatusService implements ProcessOrderOutboxUseCase {

    private final OutboxPort outboxPort;
    private final OutboxEventHandlerRegistry outboxEventHandlerRegistry;

    public OrderOutboxStatusService(
            OutboxPort outboxPort,
            OutboxEventHandlerRegistry outboxEventHandlerRegistry
    ) {
        this.outboxPort = outboxPort;
        this.outboxEventHandlerRegistry = outboxEventHandlerRegistry;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventDto> findPending() {
        return outboxPort.findPending();
    }

    // Giữ khóa event đến khi handler nhận ACK và transaction cập nhật trạng thái hoàn tất.
    @Override
    @Transactional
    public boolean processEvent(OutboxEventDto candidate) {
        // Danh sách đã đọc có thể cũ; chỉ instance giữ khóa mới được chạy handler.
        Optional<OutboxEventDto> lockedEvent = outboxPort.lockPendingEvent(candidate.eventId());
        if (lockedEvent.isEmpty()) {
            return false;
        }

        OutboxEventDto event = lockedEvent.get();
        EventHandler<OutboxEventDto> handler = outboxEventHandlerRegistry.handle(event.eventType());
        handler.handle(event);
        outboxPort.markPublished(event.eventId());
        return true;
    }

    // Lỗi phải được ghi bằng transaction riêng để không mất lịch retry theo transaction xử lý đã rollback.
    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        outboxPort.markFailed(eventId, errorMessage);
    }
}
