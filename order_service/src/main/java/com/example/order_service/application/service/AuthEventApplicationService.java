package com.example.order_service.application.service;

import com.example.order_service.application.port.in.ProcessAuthEventUseCase;
import com.example.order_service.application.registry.KafkaEventHandlerRegistry;
import com.example.order_service.application.strategy.EventHandler;
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
        log.info("Xử lý event auth thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
