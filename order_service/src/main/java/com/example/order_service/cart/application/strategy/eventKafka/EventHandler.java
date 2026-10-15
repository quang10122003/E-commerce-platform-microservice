package com.example.order_service.cart.application.strategy.eventKafka;

public interface EventHandler {
    String  eventType();
    void handle(String event);
}
