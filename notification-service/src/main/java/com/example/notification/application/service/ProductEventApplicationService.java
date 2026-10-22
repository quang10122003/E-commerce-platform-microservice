package com.example.notification.application.service;

import com.example.notification.application.port.in.ProcessProductEventUseCase;
import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import com.example.notification.application.strategy.event.EventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductEventApplicationService implements ProcessProductEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    public ProductEventApplicationService(KafkaEventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }

    // Chỉ bỏ qua event chưa hỗ trợ; lỗi nghiệp vụ phải truyền về consumer để retry.
    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler handler = eventHandlerRegistry.handle(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event product chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        log.debug("Bắt đầu xử lý event product: event_id={}, event_type={}", eventId, eventType);
        handler.handle(message);
        log.info("Xử lý event product thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
