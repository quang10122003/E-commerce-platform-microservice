package com.example.order_service.application.port.in;

public interface ProcessCheckoutEventUseCase {
    void process(String eventId, String eventType, String message);
}
