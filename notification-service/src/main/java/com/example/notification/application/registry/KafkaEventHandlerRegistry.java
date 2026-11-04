package com.example.notification.application.registry;

import com.example.notification.application.strategy.EventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KafkaEventHandlerRegistry {

    private final Map<String, EventHandler<String>> handlers;

    public KafkaEventHandlerRegistry(List<EventHandler<String>> eventHandlers) {
        this.handlers = eventHandlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        EventHandler<String>::eventType,
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Trùng handler cho eventType=" + first.eventType());
                        }
                ));
    }

    public EventHandler<String> handle(String eventType) {
        return handlers.get(eventType);
    }
}
