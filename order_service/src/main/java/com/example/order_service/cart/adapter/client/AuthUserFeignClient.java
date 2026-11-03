package com.example.order_service.cart.adapter.client;

import com.example.order_service.cart.application.dto.repone.UserInternaInfoRespone;
import com.example.order_service.order.application.dto.checkout.ShopSnapshotDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    @GetMapping("/api/internal/shops/{shopId}/info")
    ShopSnapshotDto getShopSnapshot(@PathVariable("shopId") Long shopId);

    @GetMapping("/api/internal/getInfoUser")
    UserInternaInfoRespone getInFo();
}
