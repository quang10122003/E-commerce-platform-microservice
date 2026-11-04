package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.request.CheckoutItemsRequest;
import com.example.order_service.application.dto.response.CheckoutItemsResponse;

public interface GetCheckoutItemsUseCase {
    CheckoutItemsResponse getCheckoutItems(CheckoutItemsRequest request);
}
