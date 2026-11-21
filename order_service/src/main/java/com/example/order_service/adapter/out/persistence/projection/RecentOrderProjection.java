package com.example.order_service.adapter.out.persistence.projection;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RecentOrderProjection {
    Long getOrderId();
    Long getShopId();
    String getShopName();
    String getRecipientName();
    BigDecimal getTotalAmount();
    OrderStatus getStatus();
    CheckoutPaymentMethod getPaymentMethod();
    LocalDateTime getCreatedAt();
}
