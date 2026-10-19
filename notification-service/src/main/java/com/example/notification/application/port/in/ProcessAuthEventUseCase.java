package com.example.notification.application.port.in;

public interface ProcessAuthEventUseCase {
    void process(String eventId, String eventType, String message);
}
