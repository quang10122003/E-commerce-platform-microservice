package com.example.notification.adapter.client;

import com.example.notification.application.dto.response.CurrentUserInfoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {

    // Dùng Authorization được FeignGlobalConfig chuyển tiếp từ request hiện tại.
    @GetMapping("/api/internal/getInfoUser")
    CurrentUserInfoResponse getInfoUser();
}
