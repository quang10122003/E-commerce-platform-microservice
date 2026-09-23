package com.example.auth_service.application.registry;

import com.example.auth_service.application.strategy.outbox.OutboxEventHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// Định tuyến outbox event đến đúng strategy theo eventType.
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

    // Tìm và trả về strategy phụ trách event type.
    public OutboxEventHandler handle(String eventType) {
        return Optional.ofNullable(handlers.get(eventType))
                .orElseThrow(() -> new IllegalStateException(
                        "Chưa đăng ký handler cho eventType=" + eventType));
    }
}
