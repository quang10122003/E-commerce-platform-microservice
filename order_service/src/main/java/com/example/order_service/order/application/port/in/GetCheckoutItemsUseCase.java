package com.example.order_service.order.application.port.in;

import com.example.order_service.order.application.dto.request.CheckoutItemsRequest;
import com.example.order_service.order.application.dto.repone.CheckoutItemsResponse;

public interface GetCheckoutItemsUseCase {
    CheckoutItemsResponse getCheckoutItems(CheckoutItemsRequest request);
}
