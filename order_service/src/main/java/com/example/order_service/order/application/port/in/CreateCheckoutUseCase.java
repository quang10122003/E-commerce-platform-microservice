package com.example.order_service.order.application.port.in;

import com.example.order_service.order.application.dto.repone.CreateCheckoutResponse;
import com.example.order_service.order.application.dto.request.CreateCheckoutRequest;

public interface CreateCheckoutUseCase {
    CreateCheckoutResponse createCheckout(CreateCheckoutRequest request, String idempotencyKey, String clientIp);
}
