package com.example.order_service.order.application.registry;

import com.example.order_service.order.application.strategy.eventKafka.EventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CheckoutEventHandlerRegistry {

    private final Map<String, EventHandler> handlers;

    public CheckoutEventHandlerRegistry(List<EventHandler> eventHandlers) {
        this.handlers = eventHandlers.stream().collect(Collectors.toUnmodifiableMap(
                EventHandler::eventType, Function.identity(),
                (EventHandler first, EventHandler second) -> {
                    throw new IllegalStateException("Trùng handler cho eventType=" + first.eventType());
                }));
    }

    public EventHandler getHandler(String eventType) {
        return handlers.get(eventType);
    }
}
