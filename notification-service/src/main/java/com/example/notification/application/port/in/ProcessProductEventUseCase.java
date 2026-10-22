package com.example.notification.application.port.in;

public interface ProcessProductEventUseCase {
    void process(String eventId, String eventType, String message);
}
