package com.example.producr_service.application.service;

import com.example.producr_service.application.port.in.ProcessAuthEventUseCase;
import com.example.producr_service.application.port.in.ProcessOrderEventUseCase;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import com.example.producr_service.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Event auth và order dùng chung một registry và chính sách bỏ qua event chưa hỗ trợ; topic được consumer ghi log.
@Slf4j
@RequiredArgsConstructor
public class KafkaEventApplicationService implements ProcessAuthEventUseCase, ProcessOrderEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler<String> handler = eventHandlerRegistry.getHandler(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        handler.handle(message);
        log.info("Xử lý event hoàn tất: event_id={}, event_type={}", eventId, eventType);
    }
}
