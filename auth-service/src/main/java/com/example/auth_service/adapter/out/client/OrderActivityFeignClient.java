package com.example.auth_service.adapter.out.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service")
public interface OrderActivityFeignClient {
    @GetMapping("/api/internal/users/{userId}/active-orders")
    boolean hasActiveOrders(@PathVariable("userId") Long userId);
}
