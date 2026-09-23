package com.example.notification.application.registry;

import com.example.notification.application.strategy.event.AuthEventHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// Định tuyến event auth đến đúng strategy theo eventType.
public class AuthEventHandlerRegistry {

    private final Map<String, AuthEventHandler> handlers;

    public AuthEventHandlerRegistry(List<AuthEventHandler> eventHandlers) {
        this.handlers = eventHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        AuthEventHandler::eventType,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Trùng handler cho eventType=" + first.eventType());
                        }
                ));
    }

    // Tìm và trả về strategy phụ trách event type.
    public AuthEventHandler handle(String eventType) {
        return handlers.get(eventType);
    }
}
