package com.example.order_service.order.domain.model;

public enum OrderStatus {
    PENDING_CONFIRMATION,
    PENDING_PICKUP,
    SHIPPING,
    DONE,
    CANCEL
}
