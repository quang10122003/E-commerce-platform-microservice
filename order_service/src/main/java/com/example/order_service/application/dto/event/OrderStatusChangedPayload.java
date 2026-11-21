package com.example.order_service.application.dto.event;

import com.example.order_service.domain.model.Checkout;
import com.example.order_service.domain.model.Order;
import com.example.order_service.domain.model.OrderStatus;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

// Đơn đổi trạng thái (shop chuyển hoặc job tự hoàn thành); Notification gửi mail cho người mua theo trạng thái mới.
public record OrderStatusChangedPayload(
        UUID eventId, String eventType, Long orderId, Long checkoutId, String shopName, String buyerEmail,
        String buyerFullName, OrderStatus status, OffsetDateTime changedAt) {
    public static final String EVENT_TYPE = "ORDER_STATUS_CHANGED";

    // Dùng chung cho API shop và job để hai luồng phát event cùng một dạng.
    public static OrderStatusChangedPayload of(Order order, Checkout checkout, ZoneId zone) {
        return new OrderStatusChangedPayload(UUID.randomUUID(), EVENT_TYPE, order.getId(), order.getCheckoutId(),
                order.getShopNameSnapshot(), checkout.getBuyerEmailSnapshot(), checkout.getRecipientNameSnapshot(),
                order.getStatus(), order.getUpdatedAt().atZone(zone).toOffsetDateTime());
    }
}
