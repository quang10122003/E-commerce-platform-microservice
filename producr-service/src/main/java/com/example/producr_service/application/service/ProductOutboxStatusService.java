package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;
import com.example.producr_service.application.port.in.ProcessProductOutboxUseCase;
import com.example.producr_service.application.port.out.outbox.OutboxPort;
import com.example.producr_service.application.registry.OutboxEventHandlerRegistry;
import com.example.producr_service.application.strategy.outbox.OutboxEventHandler;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Điều phối xử lý event outbox và quản lý transaction cho từng lần cập nhật trạng thái.
public class ProductOutboxStatusService implements ProcessProductOutboxUseCase {

    // Port đọc và cập nhật trạng thái event outbox.
    private final OutboxPort outboxPort;
    // Registry chọn handler tương ứng với loại event.
    private final OutboxEventHandlerRegistry outboxEventHandlerRegistry;

    // Nhận port lưu trữ và registry handler từ cấu hình ứng dụng.
    public ProductOutboxStatusService(
            OutboxPort outboxPort,
            OutboxEventHandlerRegistry outboxEventHandlerRegistry
    ) {
        this.outboxPort = outboxPort;
        this.outboxEventHandlerRegistry = outboxEventHandlerRegistry;
    }

    // Đọc các event đã đến hạn xử lý trong transaction chỉ đọc.
    @Override
    @Transactional(readOnly = true)
    public List<OutboxEventDto> findPending() {
        return outboxPort.findPending();
    }

    // Xử lý event và ghi nhận thành công trong cùng transaction để tải đủ dữ liệu sản phẩm.
    @Override
    @Transactional
    public void processEvent(OutboxEventDto event) {
        OutboxEventHandler handler = outboxEventHandlerRegistry.handle(event.eventType());
        handler.handle(event);
        outboxPort.markPublished(event.eventId());
    }

    // Ghi nhận lỗi và lịch thử lại của event trong một transaction riêng.
    @Override
    @Transactional
    public void markFailed(UUID eventId, String errorMessage) {
        outboxPort.markFailed(eventId, errorMessage);
    }
}
