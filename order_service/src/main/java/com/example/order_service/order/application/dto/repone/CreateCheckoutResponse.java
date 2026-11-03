package com.example.order_service.order.application.dto.repone;

import com.example.order_service.order.domain.model.CheckoutStatus;

public record CreateCheckoutResponse(Long checkoutId, CheckoutStatus status) {
}
