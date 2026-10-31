package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.request.VariantQuoteRequest;
import com.example.producr_service.application.dto.response.VariantQuoteResponse;

public interface GetVariantQuoteUseCase {
    VariantQuoteResponse getVariantQuote(VariantQuoteRequest request);
}
