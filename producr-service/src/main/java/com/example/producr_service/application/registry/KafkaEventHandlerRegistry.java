package com.example.producr_service.application.registry;

import com.example.producr_service.application.strategy.eventKafka.EventHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KafkaEventHandlerRegistry {
    private final Map<String, EventHandler> handlers;

    public KafkaEventHandlerRegistry(List<EventHandler> eventHandlers) {
        this.handlers = eventHandlers.stream().collect(Collectors.toUnmodifiableMap(
                EventHandler::eventType, Function.identity(), (first, second) -> {
                    throw new IllegalStateException("Trùng handler cho eventType=" + first.eventType());
                }));
    }

    public EventHandler getHandler(String eventType) {
        return handlers.get(eventType);
    }
}
