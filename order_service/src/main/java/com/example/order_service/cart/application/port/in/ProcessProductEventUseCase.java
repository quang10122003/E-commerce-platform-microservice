package com.example.order_service.cart.application.port.in;

public interface ProcessProductEventUseCase {
    void process(String eventId, String eventType, String message);
}
