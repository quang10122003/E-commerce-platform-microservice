package com.example.order_service.order.application.strategy.payment;

import com.example.order_service.order.application.dto.saga.CheckoutRequestedOutboxPayload;
import com.example.order_service.order.application.service.CheckoutService;
import com.example.order_service.order.domain.model.CheckoutPaymentMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CodCheckoutHandler implements CheckoutPaymentHandler {

    private final CheckoutService checkoutService;

    @Override
    public CheckoutPaymentMethod paymentMethod() {
        return CheckoutPaymentMethod.COD;
    }

    @Override
    public void handle(CheckoutRequestedOutboxPayload event) {
        checkoutService.processCodCheckout(event);
    }
}
