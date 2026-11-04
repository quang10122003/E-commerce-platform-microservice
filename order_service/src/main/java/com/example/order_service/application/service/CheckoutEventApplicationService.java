package com.example.order_service.application.service;

import com.example.order_service.application.strategy.EventHandler;
import com.example.order_service.application.port.in.ProcessCheckoutEventUseCase;
import com.example.order_service.application.registry.KafkaEventHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CheckoutEventApplicationService implements ProcessCheckoutEventUseCase {

    private final KafkaEventHandlerRegistry eventHandlerRegistry;

    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler<String> handler = eventHandlerRegistry.getHandler(eventType);
        if (handler == null) {
            log.warn("Bỏ qua event order chưa hỗ trợ: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        log.debug("Bắt đầu xử lý event order: event_id={}, event_type={}", eventId, eventType);
        handler.handle(message);
        log.info("Xử lý event order thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
