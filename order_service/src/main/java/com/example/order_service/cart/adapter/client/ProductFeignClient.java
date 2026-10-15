package com.example.order_service.cart.adapter.client;

import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "producr-service")
public interface ProductFeignClient {
    @PostMapping("/api/internal/cart-info")
    List<CartInfoRepone> getCartInfo(@RequestBody List<Long> variantIds);
}
