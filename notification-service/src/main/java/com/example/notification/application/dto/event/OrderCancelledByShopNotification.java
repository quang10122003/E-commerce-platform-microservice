package com.example.notification.application.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

// Order Service phát khi shop hủy đơn; dùng để gửi mail báo người mua.
public record OrderCancelledByShopNotification(
        UUID eventId, String eventType, Long orderId, Long checkoutId, String shopName, String buyerEmail,
        String buyerFullName, String cancelReason, BigDecimal totalAmount, OffsetDateTime cancelledAt
) {
    public static final String EVENT_TYPE = "ORDER_CANCELLED_BY_SHOP";

    public OrderCancelledByShopNotification {
        Objects.requireNonNull(eventId);
        if (!EVENT_TYPE.equals(eventType) || orderId == null || orderId <= 0 || totalAmount == null || cancelledAt == null) {
            throw new IllegalArgumentException("Event shop hủy đơn không hợp lệ");
        }
    }
}
