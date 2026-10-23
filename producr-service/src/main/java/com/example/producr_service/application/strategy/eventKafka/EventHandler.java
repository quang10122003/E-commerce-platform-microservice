package com.example.producr_service.application.strategy.eventKafka;

public interface EventHandler {
    String eventType();
    void handle(String message);
}
