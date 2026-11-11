package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderCancelledBy;
import com.example.order_service.domain.model.OrderCancelReason;
import com.example.order_service.domain.model.OrderStatus;
import com.example.order_service.domain.model.PaymentStatus;
import com.example.order_service.domain.model.PriceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record UserOrderResponse(
        Long orderId, Long checkoutId, OrderStatus status, Shop shop, PriceType priceType,
        BigDecimal totalAmount, Payment payment, List<Item> items, OrderCancelledBy cancelledBy, OrderCancelReason cancelReason,
        OffsetDateTime cancelledAt, OffsetDateTime confirmedAt, OffsetDateTime shippedAt, OffsetDateTime deliveredAt, OffsetDateTime createdAt) {

    public record Shop(Long shopId, String shopName, String shopLogoUrl) {
    }

    // paidAt là mốc VNPay xác nhận thanh toán; đơn COD luôn null.
    public record Payment(CheckoutPaymentMethod method, PaymentStatus status, String paymentUrl,
                          OffsetDateTime expiresAt, OffsetDateTime paidAt) {
    }

    public record Item(Long orderItemId, Long productId, Long variantId, String productName,
                       String variantAttributes, String imageUrl, int quantity, BigDecimal originalUnitPrice,
                       BigDecimal unitPrice, BigDecimal totalAmount) {
    }
}
