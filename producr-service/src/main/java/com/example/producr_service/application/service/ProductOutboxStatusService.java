package com.example.producr_service.application.service;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.port.in.ProcessProductOutboxUseCase;
import com.example.producr_service.application.port.in.PublishCheckoutOutboxUseCase;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.registry.OutboxEventHandlerRegistry;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public class ProductOutboxStatusService implements ProcessProductOutboxUseCase, PublishCheckoutOutboxUseCase {

    private final OutboxPort outboxPort;
    private final OutboxEventHandlerRegistry outboxEventHandlerRegistry;

    public ProductOutboxStatusService(
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

    // Handler cần transaction còn mở để tải đủ dữ liệu trước khi đánh dấu event thành công.
    @Override
    @Transactional
    public boolean processEvent(OutboxEventDto candidate) {
        return lockAndPublish(candidate);
    }

    // Chỉ đẩy ngay kết quả trừ kho của checkout để rút ngắn saga; sự kiện sản phẩm vẫn chờ bộ quét như cũ.
    @Override
    @Transactional
    public void publishCheckoutEvent(OutboxEventDto event) {
        if (Constant.CHECKOUT.equals(event.aggregateType())) {
            lockAndPublish(event);
        }
    }

    private boolean lockAndPublish(OutboxEventDto candidate) {
        // Danh sách đã đọc có thể cũ; chỉ instance giữ khóa mới được chạy handler, nên gửi ngay và bộ quét không gửi trùng.
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
