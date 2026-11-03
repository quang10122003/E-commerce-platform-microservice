package com.example.order_service.order.application.port.out.client;

import com.example.order_service.order.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.order.application.dto.repone.CheckoutQuoteResponse;

public interface ProductQuotePort {
    CheckoutQuoteResponse getProductQuote(CheckoutQuoteRequest request);
}
