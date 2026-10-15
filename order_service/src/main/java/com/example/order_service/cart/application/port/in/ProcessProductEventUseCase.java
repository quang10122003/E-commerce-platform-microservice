package com.example.order_service.cart.application.port.in;

// Boundary tiếp nhận event product từ adapter, không phụ thuộc Kafka.
public interface ProcessProductEventUseCase {
    // Điều phối event đã đọc metadata sang strategy phù hợp.
    void process(String eventId, String eventType, String message);
}
