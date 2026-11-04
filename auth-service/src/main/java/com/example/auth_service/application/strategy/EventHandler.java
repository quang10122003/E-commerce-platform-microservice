package com.example.auth_service.application.strategy;

public interface EventHandler<T> {
    String eventType();

    void handle(T event);
}
