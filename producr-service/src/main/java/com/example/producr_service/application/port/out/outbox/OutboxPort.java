package com.example.producr_service.application.port.out.outbox;

import com.example.producr_service.application.dto.outbox.OutboxEventDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Cổng lưu và cập nhật trạng thái event outbox tổng quát.
public interface OutboxPort {

    // Lưu event cùng transaction nghiệp vụ phát sinh event.
    void save(OutboxEventDto event);

    // Lấy các event đang chờ xử lý và đã đến thời điểm retry.
    List<OutboxEventDto> findPending();

    // Khóa event còn chờ xử lý trước khi gọi handler.
    Optional<OutboxEventDto> lockPendingEvent(UUID eventId);

    // Đánh dấu event đã xử lý thành công.
    void markPublished(UUID eventId);

    // Ghi nhận lỗi và lên lịch retry hoặc chuyển event sang FAILED.
    void markFailed(UUID eventId, String errorMessage);
}
