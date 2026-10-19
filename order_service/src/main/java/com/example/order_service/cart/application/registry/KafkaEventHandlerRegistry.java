package com.example.order_service.cart.application.registry;

import com.example.order_service.cart.application.strategy.eventKafka.EventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
public class KafkaEventHandlerRegistry {
    private final Map<String, EventHandler> handlers;

    public KafkaEventHandlerRegistry(List<EventHandler> eventHandlers) {
        this.handlers = eventHandlers.stream().collect(Collectors.toUnmodifiableMap(
                EventHandler::eventType, Function.identity(),
                (first, second) -> {
                    throw new IllegalStateException(
                            "Trùng handler cho eventType=" + first.eventType());
                }
        ));
    }

    public EventHandler getHandler(String eventType) {
        return handlers.get(eventType);
    }
}
