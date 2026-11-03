package com.example.order_service.order.application.port.in;

import com.example.order_service.order.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.order.application.dto.repone.CheckoutQuoteResponse;

public interface GetCheckoutQuoteUseCase {
    CheckoutQuoteResponse getCheckoutQuote(CheckoutQuoteRequest request);
}
