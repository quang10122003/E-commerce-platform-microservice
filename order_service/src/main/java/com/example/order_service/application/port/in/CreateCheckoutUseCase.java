package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.CreateCheckoutResponse;
import com.example.order_service.application.dto.request.CreateCheckoutRequest;

public interface CreateCheckoutUseCase {
    CreateCheckoutResponse createCheckout(CreateCheckoutRequest request, String idempotencyKey, String clientIp);
}
