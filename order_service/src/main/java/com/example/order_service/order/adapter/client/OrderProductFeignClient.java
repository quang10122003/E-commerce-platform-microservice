package com.example.order_service.order.adapter.client;

import com.example.order_service.order.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.order.application.dto.repone.CheckoutQuoteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "producr-service", contextId = "orderProductQuoteClient")
public interface OrderProductFeignClient {
    @PostMapping(value = "/api/internal/variants/quote", consumes = MediaType.APPLICATION_JSON_VALUE)
    CheckoutQuoteResponse getProductQuote(@RequestBody CheckoutQuoteRequest request);
}
