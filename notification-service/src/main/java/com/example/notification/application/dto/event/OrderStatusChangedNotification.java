package com.example.notification.application.dto.event;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

// Order Service phát khi shop chuyển trạng thái đơn; dùng để gửi mail báo người mua.
public record OrderStatusChangedNotification(
        UUID eventId, String eventType, Long orderId, Long checkoutId, String shopName, String buyerEmail,
        String buyerFullName, String status, OffsetDateTime changedAt
) {
    public static final String EVENT_TYPE = "ORDER_STATUS_CHANGED";

    public OrderStatusChangedNotification {
        Objects.requireNonNull(eventId);
        if (!EVENT_TYPE.equals(eventType) || orderId == null || orderId <= 0 || status == null || changedAt == null) {
            throw new IllegalArgumentException("Event đổi trạng thái đơn không hợp lệ");
        }
    }
}
