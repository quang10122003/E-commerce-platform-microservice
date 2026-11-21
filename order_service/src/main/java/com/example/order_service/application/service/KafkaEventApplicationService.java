package com.example.order_service.application.service;

import com.example.order_service.application.port.in.ProcessAuthEventUseCase;
import com.example.order_service.application.port.in.ProcessCheckoutEventUseCase;
import com.example.order_service.application.port.in.ProcessProductEventUseCase;
import com.example.order_service.application.registry.KafkaEventHandlerRegistry;
import com.example.order_service.application.strategy.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Event auth, checkout và product dùng chung registry và chính sách bỏ qua event chưa hỗ trợ; topic được consumer ghi log.
@Slf4j
@RequiredArgsConstructor
public class KafkaEventApplicationService implements ProcessAuthEventUseCase, ProcessCheckoutEventUseCase,
        ProcessProductEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    // Chỉ bỏ qua event chưa hỗ trợ; lỗi handler phải truyền về adapter để retry.
    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler<String> handler = eventHandlerRegistry.getHandler(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        log.debug("Bắt đầu xử lý event: event_id={}, event_type={}", eventId, eventType);
        handler.handle(message);
        log.info("Xử lý event thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
