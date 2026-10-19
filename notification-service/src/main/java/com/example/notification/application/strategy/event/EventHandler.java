package com.example.notification.application.strategy.event;

public interface EventHandler {

    String eventType();

    void handle(String message);
}
