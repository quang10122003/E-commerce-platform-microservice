package com.example.notification.adapter.client;

import com.example.notification.adapter.client.dto.ShopDisplayInfoResponse;
import com.example.notification.adapter.client.dto.UserDisplayInfoResponse;
import com.example.notification.application.dto.response.CurrentUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// Dùng Authorization được FeignGlobalConfig chuyển tiếp từ request hiện tại.
@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    // Lấy thông tin các shop theo danh sách ID.
    @PostMapping("/api/internal/shop-info")
    List<ShopDisplayInfoResponse> getShopInfos(
            @RequestBody List<Long> shopIds);

    // Lấy thông tin khách hàng theo danh sách ID để hiển thị trong chat.
    @PostMapping("/api/internal/user-info")
    List<UserDisplayInfoResponse> getUserDisplayInfosByIds(
            @RequestBody List<Long> userIds);

    // Lấy thông tin người dùng bằng phiên đăng nhập hiện tại.
    @GetMapping("/api/internal/getInfoUser")
    CurrentUserInfoResponse getInfoUser();
}
