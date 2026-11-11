package com.example.order_service.application.dto.event;

import com.example.order_service.domain.model.OrderCancelReason;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Notification gửi mail báo người mua; mang sẵn snapshot người mua để không phải gọi ngược Order Service.
public record OrderCancelledByShopPayload(
        UUID eventId, String eventType, Long orderId, Long checkoutId, String shopName, String buyerEmail,
        String buyerFullName, OrderCancelReason cancelReason, BigDecimal totalAmount, OffsetDateTime cancelledAt) {
    public static final String EVENT_TYPE = "ORDER_CANCELLED_BY_SHOP";
}
