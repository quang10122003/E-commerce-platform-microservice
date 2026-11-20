package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.CreateCheckoutResponse;

public interface GetCheckoutUseCase {
    CreateCheckoutResponse getCheckout(Long checkoutId);
}
