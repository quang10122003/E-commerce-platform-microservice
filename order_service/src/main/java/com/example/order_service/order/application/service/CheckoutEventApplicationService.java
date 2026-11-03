package com.example.order_service.order.application.service;

import com.example.order_service.order.application.port.in.ProcessCheckoutEventUseCase;
import com.example.order_service.order.application.registry.CheckoutEventHandlerRegistry;
import com.example.order_service.order.application.strategy.eventKafka.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class CheckoutEventApplicationService implements ProcessCheckoutEventUseCase {

    private final CheckoutEventHandlerRegistry eventHandlerRegistry;

    @Override
    public void process(String eventId, String eventType, String message) {
        EventHandler handler = eventHandlerRegistry.getHandler(eventType);
        // Topic dùng chung với event giỏ hàng nên chỉ xử lý loại event thuộc checkout.
        if (handler == null) {
            log.warn("Bỏ qua event không thuộc checkout: event_id={}, event_type={}", eventId, eventType);
            return;
        }
        log.debug("Bắt đầu xử lý event checkout: event_id={}, event_type={}", eventId, eventType);
        handler.handle(message);
        log.info("Xử lý event checkout thành công: event_id={}, event_type={}", eventId, eventType);
    }
}
