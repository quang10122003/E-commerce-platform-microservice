package com.example.order_service.application.port.out.client;

import com.example.order_service.application.dto.response.ProductVariantInfoResponse;
import com.example.order_service.application.dto.request.ProductQuoteRequest;
import com.example.order_service.application.dto.response.ProductQuoteResponse;

import java.util.List;

public interface ProductClientPort {
    ProductQuoteResponse getProductQuote(ProductQuoteRequest request);

    List<ProductVariantInfoResponse> getVariantInfo(List<Long> variantIds);
}
