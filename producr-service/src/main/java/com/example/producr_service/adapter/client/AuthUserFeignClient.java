package com.example.producr_service.adapter.client;

import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    @GetMapping("/api/internal/getInfoUser")
    UserInternaInfoRespone getCurrentUserId();
}
