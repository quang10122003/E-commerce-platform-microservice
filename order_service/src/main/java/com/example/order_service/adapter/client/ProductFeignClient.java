package com.example.order_service.adapter.client;

import com.example.order_service.application.dto.response.ProductVariantInfoResponse;
import com.example.order_service.application.dto.request.ProductQuoteRequest;
import com.example.order_service.application.dto.response.ProductQuoteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;

import java.util.List;

@FeignClient(name = "producr-service")
public interface ProductFeignClient {
    @PostMapping(value = "/api/internal/variants/quote", consumes = MediaType.APPLICATION_JSON_VALUE)
    ProductQuoteResponse getProductQuote(@RequestBody ProductQuoteRequest request);

    @PostMapping("/api/internal/variants/info")
    List<ProductVariantInfoResponse> getVariantInfo(@RequestBody List<Long> variantIds);
}
