package com.example.order_service.application.dto.response;

import com.example.order_service.domain.model.CheckoutPaymentMethod;
import com.example.order_service.domain.model.CheckoutStatus;

import java.util.List;

public record CreateCheckoutResponse(Long checkoutId, CheckoutStatus status, CheckoutPaymentMethod paymentMethod,
                                     List<Long> orderIds, String urlPayment, String reason) {
    public CreateCheckoutResponse {
        orderIds = orderIds == null ? List.of() : List.copyOf(orderIds);
    }
}
