package com.example.producr_service.application.port.in;

public interface ProcessAuthEventUseCase {

    void process(String eventId, String eventType, String message);
}
