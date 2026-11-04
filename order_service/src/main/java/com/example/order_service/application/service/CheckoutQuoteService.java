package com.example.order_service.application.service;

import com.example.order_service.application.port.out.client.ProductClientPort;
import com.example.order_service.application.dto.request.ProductQuoteRequest;
import com.example.order_service.application.dto.response.ProductQuoteResponse;
import com.example.order_service.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.application.dto.response.CheckoutQuoteResponse;
import com.example.order_service.application.port.in.GetCheckoutQuoteUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutQuoteService implements GetCheckoutQuoteUseCase {
    private final ProductClientPort productClientPort;

    @Override
    public CheckoutQuoteResponse getCheckoutQuote(CheckoutQuoteRequest request) {
        ProductQuoteRequest pricingRequest = new ProductQuoteRequest(request.items().stream()
                .map(item -> new ProductQuoteRequest.Item(item.variantId(), item.quantity()))
                .toList());
        ProductQuoteResponse quote = productClientPort.getProductQuote(pricingRequest);
        return new CheckoutQuoteResponse(quote.totalAmount());
    }
}
