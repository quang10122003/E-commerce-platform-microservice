package com.example.order_service.application.strategy.payment;

import com.example.order_service.application.dto.saga.CheckoutRequestedOutboxPayload;
import com.example.order_service.domain.model.CheckoutPaymentMethod;

public interface CheckoutPaymentHandler {
    CheckoutPaymentMethod paymentMethod();
    void handle(CheckoutRequestedOutboxPayload event);
}
