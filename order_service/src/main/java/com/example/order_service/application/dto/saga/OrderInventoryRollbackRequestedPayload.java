package com.example.order_service.application.dto.saga;

import java.time.OffsetDateTime;
import java.util.UUID;

// Yêu cầu Product hoàn tồn kho của một order; Product lọc item theo shopId + sale từ kết quả trừ tồn đã lưu.
public record OrderInventoryRollbackRequestedPayload(
        UUID eventId, String eventType, Long checkoutId, Long orderId, Long shopId, boolean sale,
        UUID inventorySucceededEventId, UUID checkoutSucceededEventId, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "ORDER_INVENTORY_ROLLBACK_REQUESTED";
}
