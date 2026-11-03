package com.example.notification.application.port.in;

public interface ProcessOrderEventUseCase {
    void process(String eventId, String eventType, String message);
}
