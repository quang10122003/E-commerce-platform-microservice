package com.example.order_service.cart.application.service;

import com.example.order_service.cart.application.port.in.ProcessProductEventUseCase;
import com.example.order_service.cart.application.registry.KafkaEventHandlerRegistry;
import com.example.order_service.cart.application.strategy.eventKafka.EventHandler;
import lombok.extern.slf4j.Slf4j;

// Điều phối event product trong application, giữ consumer độc lập với registry và handler.
@Slf4j
public class ProductEventApplicationService implements ProcessProductEventUseCase {
    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    // Nhận registry đã được lắp ghép tại infrastructure.
    public ProductEventApplicationService(KafkaEventHandlerRegistry eventHandlerRegistry) {
        this.eventHandlerRegistry = eventHandlerRegistry;
    }

    // Chỉ bỏ qua loại event chưa hỗ trợ; lỗi handler phải truyền về adapter để retry.
    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler handler = eventHandlerRegistry.getHandler(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event product chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        log.debug("Bắt đầu xử lý event product: event_id={}, event_type={}", eventId, eventType);
        handler.handle(message);
        log.info("Xử lý event product thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
