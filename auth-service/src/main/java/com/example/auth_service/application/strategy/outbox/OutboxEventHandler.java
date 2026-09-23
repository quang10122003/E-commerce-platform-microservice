package com.example.auth_service.application.strategy.outbox;

import com.example.auth_service.application.DTO.OutboxEventDto;

// Định nghĩa strategy phát hành một loại event outbox.
public interface OutboxEventHandler {

    // Trả về mã event mà strategy phụ trách.
    String eventType();

    // Phát hành event đến hệ thống message broker.
    void handle(OutboxEventDto event);
}
