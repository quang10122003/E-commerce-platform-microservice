package com.example.producr_service.application.service;

import com.example.producr_service.application.port.in.ProcessOrderEventUseCase;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.strategy.eventKafka.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class OrderEventApplicationService implements ProcessOrderEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler handler = eventHandlerRegistry.getHandler(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event order chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        handler.handle(message);
        log.info("Xử lý event order hoàn tất: event_id={}, event_type={}", eventId, eventType);
    }
}
