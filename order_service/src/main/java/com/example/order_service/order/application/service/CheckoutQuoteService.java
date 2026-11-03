package com.example.order_service.order.application.service;

import com.example.order_service.order.application.port.out.client.ProductQuotePort;
import com.example.order_service.order.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.order.application.dto.repone.CheckoutQuoteResponse;
import com.example.order_service.order.application.port.in.GetCheckoutQuoteUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CheckoutQuoteService implements GetCheckoutQuoteUseCase {
    private final ProductQuotePort productQuotePort;

    @Override
    public CheckoutQuoteResponse getCheckoutQuote(CheckoutQuoteRequest request) {
        return productQuotePort.getProductQuote(request);
    }
}
