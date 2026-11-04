package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.request.ProductQuoteRequest;
import com.example.order_service.application.dto.response.ProductQuoteResponse;

public interface GetCartQuoteUseCase {
    ProductQuoteResponse getCartQuote(ProductQuoteRequest request);
}
