package com.example.auth_service.application.port.out;

import java.util.List;
import java.util.UUID;

import com.example.auth_service.application.DTO.OutboxEventDto;
import com.example.auth_service.domain.model.OutboxEvent;

public interface OutboxEventPort {

    // Ghi sự kiện đăng ký vào outbox.
    void save(OutboxEvent event);

    // Lấy các sự kiện đang chờ để adapter Kafka publish.
    List<OutboxEventDto> findPending();

    // Đánh dấu event đã publish thành công.
    void markPublished(UUID eventId);

    // Tăng retry và lưu lỗi của lần publish gần nhất.
    void markFailed(UUID eventId, String errorMessage);

}
