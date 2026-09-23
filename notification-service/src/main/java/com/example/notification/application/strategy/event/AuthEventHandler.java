package com.example.notification.application.strategy.event;

// Định nghĩa strategy xử lý một loại event nhận từ auth service.
public interface AuthEventHandler {

    // Trả về mã event mà strategy phụ trách.
    String eventType();

    // Xử lý message JSON của event.
    void handle(String message);
}
