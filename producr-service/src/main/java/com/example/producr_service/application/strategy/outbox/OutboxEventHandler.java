package com.example.producr_service.application.strategy.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;

// Định nghĩa strategy xử lý một loại event outbox.
public interface OutboxEventHandler {

    // Trả về mã event mà strategy phụ trách.
    String eventType();

    // Xử lý aggregate tương ứng với event.
    void handle(OutboxEventDto event);
}
