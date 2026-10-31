package com.example.order_service.cart.application.port.out.client;

import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import com.example.order_service.cart.application.dto.request.CartQuoteRequest;
import com.example.order_service.cart.application.dto.repone.CartQuoteResponse;

import java.util.List;

public interface ProductClientPort {
    CartQuoteResponse getCartQuote(CartQuoteRequest request);

    List<CartInfoRepone> getCartInfo(List<Long> variantIds);
}
