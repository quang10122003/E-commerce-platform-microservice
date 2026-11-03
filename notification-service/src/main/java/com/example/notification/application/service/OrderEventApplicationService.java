package com.example.notification.application.service;

import com.example.notification.application.port.in.ProcessOrderEventUseCase;
import com.example.notification.application.registry.KafkaEventHandlerRegistry;
import com.example.notification.application.strategy.event.EventHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderEventApplicationService implements ProcessOrderEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    public OrderEventApplicationService(KafkaEventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }

    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler handler = eventHandlerRegistry.handle(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event order chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        log.debug("Bắt đầu xử lý event order: event_id={}, event_type={}", eventId, eventType);
        handler.handle(message);
        log.info("Xử lý event order thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
