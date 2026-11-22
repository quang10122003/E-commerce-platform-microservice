package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.outbox.OutboxEventDto;

public interface PublishCheckoutOutboxUseCase {
    // Gọi sau khi transaction ghi outbox đã commit; lỗi để lại event PENDING cho bộ quét gửi lại.
    void publishCheckoutEvent(OutboxEventDto event);
}
