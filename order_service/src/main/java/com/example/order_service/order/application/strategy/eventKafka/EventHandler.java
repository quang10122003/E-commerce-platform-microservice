package com.example.order_service.order.application.strategy.eventKafka;

public interface EventHandler {
    String eventType();
    void handle(String event);
}
