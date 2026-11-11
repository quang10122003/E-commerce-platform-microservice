package com.example.producr_service.application.dto.saga;

import java.time.OffsetDateTime;
import java.util.UUID;

// Order Service yêu cầu hoàn tồn kho của một order bị user hủy; shopId + sale xác định item của order.
public record OrderInventoryRollbackRequestedPayload(
        UUID eventId, String eventType, Long checkoutId, Long orderId, Long shopId, boolean sale,
        UUID inventorySucceededEventId, UUID checkoutSucceededEventId, OffsetDateTime occurredAt) {
    public static final String EVENT_TYPE = "ORDER_INVENTORY_ROLLBACK_REQUESTED";
}
