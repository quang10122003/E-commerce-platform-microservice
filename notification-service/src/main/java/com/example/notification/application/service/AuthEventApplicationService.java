package com.example.notification.application.service;

import com.example.notification.application.port.in.ProcessAuthEventUseCase;
import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import com.example.notification.application.strategy.event.EventHandler;
import lombok.extern.slf4j.Slf4j;

// Điều phối strategy trong application để adapter chỉ phụ thuộc input port.
@Slf4j
public class AuthEventApplicationService implements ProcessAuthEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    // Nhận registry đã được đăng ký tại infrastructure.
    public AuthEventApplicationService(KafkaEventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }

    // Giữ chính sách bỏ qua event chưa hỗ trợ và truyền lỗi nghiệp vụ về consumer.
    @Override
    public void process(String eventId, String eventType, String message) {
        // Định tuyến event sang strategy phù hợp để không phải mở rộng consumer khi thêm event mới.
        EventHandler handler = eventHandlerRegistry.handle(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event auth chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        log.debug("Bắt đầu xử lý event auth: event_id={}, event_type={}", eventId, eventType);
        handler.handle(message);
        log.info("Xử lý event auth thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
