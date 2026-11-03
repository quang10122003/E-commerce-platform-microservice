package com.example.notification.adapter.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service")
public interface CheckoutFeignClient {
    @GetMapping("/api/internal/checkouts/{checkoutId}/access")
    void checkAccess(@PathVariable("checkoutId") Long checkoutId);
}
