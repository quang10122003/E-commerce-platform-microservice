package com.example.notification.application.port.in;

// Boundary tiếp nhận event auth, không đưa kiểu Kafka vào application.
public interface ProcessAuthEventUseCase {
    // Điều phối event đã đọc metadata sang strategy phù hợp.
    void process(String eventId, String eventType, String message);
}
