package com.example.notification.application.service;

import com.example.notification.application.port.in.ProcessAuthEventUseCase;
import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import com.example.notification.application.strategy.event.EventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthEventApplicationService implements ProcessAuthEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;
    
    public AuthEventApplicationService(KafkaEventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }

    // Chỉ bỏ qua event chưa hỗ trợ; lỗi nghiệp vụ phải truyền về consumer để retry.
    @Override
    public void process(String eventId, String eventType, String message) {
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
