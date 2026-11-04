package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.CheckoutStatus;

public record CreateCheckoutResponse(Long checkoutId, CheckoutStatus status, String urlPayment) {
}
