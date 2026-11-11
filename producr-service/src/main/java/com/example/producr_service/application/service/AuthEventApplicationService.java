package com.example.producr_service.application.service;

import com.example.producr_service.application.strategy.EventHandler;
import com.example.producr_service.application.port.in.ProcessAuthEventUseCase;
import com.example.producr_service.application.registry.KafkaEventHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AuthEventApplicationService implements ProcessAuthEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler<String> handler = eventHandlerRegistry.getHandler(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event auth chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        handler.handle(message);
        log.info("Xử lý event auth hoàn tất: event_id={}, event_type={}", eventId, eventType);
    }
}
