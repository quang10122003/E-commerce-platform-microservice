package com.example.order_service.order.application.port.in;

public interface ProcessCheckoutEventUseCase {
    void process(String eventId, String eventType, String message);
}
