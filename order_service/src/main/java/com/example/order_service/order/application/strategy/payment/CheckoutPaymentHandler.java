package com.example.order_service.order.application.strategy.payment;

import com.example.order_service.order.application.dto.saga.CheckoutRequestedOutboxPayload;
import com.example.order_service.order.domain.model.CheckoutPaymentMethod;

public interface CheckoutPaymentHandler {
    CheckoutPaymentMethod paymentMethod();
    void handle(CheckoutRequestedOutboxPayload event);
}
