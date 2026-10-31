package com.example.order_service.cart.adapter.client;

import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import com.example.order_service.cart.application.dto.request.CartQuoteRequest;
import com.example.order_service.cart.application.dto.repone.CartQuoteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;

import java.util.List;

@FeignClient(name = "producr-service")
public interface ProductFeignClient {
    @PostMapping(value = "/api/internal/variants/quote", consumes = MediaType.APPLICATION_JSON_VALUE)
    CartQuoteResponse getCartQuote(@RequestBody CartQuoteRequest request);

    @PostMapping("/api/internal/cart-info")
    List<CartInfoRepone> getCartInfo(@RequestBody List<Long> variantIds);
}
