package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PriceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

// Đơn của shop: các trường như đơn của user kèm snapshot người mua; paymentUrl luôn null với shop.
public record ShopOrderResponse(
        Long orderId, Long checkoutId, OrderStatus status, UserOrderResponse.Shop shop, PriceType priceType,
        BigDecimal totalAmount, UserOrderResponse.Payment payment, List<UserOrderResponse.Item> items,
        OrderCancelledBy cancelledBy, OrderCancelReason cancelReason, OffsetDateTime cancelledAt,
        OffsetDateTime confirmedAt, OffsetDateTime shippedAt, OffsetDateTime deliveredAt, OffsetDateTime createdAt,
        Buyer buyer) {

    public record Buyer(String fullName, String email, String shippingAddress, String note) {
    }
}
