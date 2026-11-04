package com.example.notification.application.strategy;

public interface EventHandler<T> {
    String eventType();

    void handle(T event);
}
