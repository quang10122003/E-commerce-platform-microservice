package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.application.dto.response.CheckoutQuoteResponse;

public interface GetCheckoutQuoteUseCase {
    CheckoutQuoteResponse getCheckoutQuote(CheckoutQuoteRequest request);
}
