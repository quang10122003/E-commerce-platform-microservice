package com.example.order_service.cart.application.port.in;

import com.example.order_service.cart.application.dto.request.CartQuoteRequest;
import com.example.order_service.cart.application.dto.repone.CartQuoteResponse;

public interface GetCartQuoteUseCase {
    CartQuoteResponse getCartQuote(CartQuoteRequest request);
}
