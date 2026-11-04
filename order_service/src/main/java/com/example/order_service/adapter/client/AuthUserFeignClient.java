package com.example.order_service.adapter.client;

import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.dto.checkout.ShopSnapshotDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    @GetMapping("/api/internal/shops/{shopId}/info")
    ShopSnapshotDto getShopSnapshot(@PathVariable("shopId") Long shopId);

    @GetMapping("/api/internal/getInfoUser")
    UserInternalInfoResponse getInFo();
}
