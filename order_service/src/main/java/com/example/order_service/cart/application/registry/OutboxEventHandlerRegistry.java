package com.example.order_service.cart.application.registry;

import com.example.order_service.cart.application.strategy.outbox.OutboxEventHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OutboxEventHandlerRegistry {

    private final Map<String, OutboxEventHandler> handlers;

    public OutboxEventHandlerRegistry(List<OutboxEventHandler> eventHandlers) {
        this.handlers = eventHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        OutboxEventHandler::eventType,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Trùng handler cho eventType=" + first.eventType());
                        }
                ));
    }

    public OutboxEventHandler handle(String eventType) {
        return Optional.ofNullable(handlers.get(eventType))
                .orElseThrow(() -> new IllegalStateException(
                        "Chưa đăng ký handler cho eventType=" + eventType));
    }
}
