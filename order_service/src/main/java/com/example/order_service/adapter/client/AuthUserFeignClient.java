package com.example.order_service.adapter.client;

import com.example.order_service.application.dto.response.ShopStatusResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.order_service.application.dto.response.UserInternalInfoResponse;
import com.example.order_service.application.dto.checkout.ShopSnapshotDto;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.order_service.application.dto.response.UserAvatarResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    @GetMapping("/api/internal/shops/statuses")
    List<ShopStatusResponse> getShopStatuses(
            @RequestParam("shopIds") List<Long> shopIds);

    @GetMapping("/api/internal/shops/{shopId}/info")
    ShopSnapshotDto getShopSnapshot(@PathVariable("shopId") Long shopId);

    @GetMapping("/api/internal/getInfoUser")
    UserInternalInfoResponse getInFo();

    @PostMapping("/api/internal/user-avatars")
    List<UserAvatarResponse> getUserAvatars(@RequestBody List<Long> userIds);
}
