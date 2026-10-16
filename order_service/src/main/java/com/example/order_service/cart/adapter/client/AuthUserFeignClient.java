package com.example.order_service.cart.adapter.client;

import com.example.order_service.cart.application.dto.repone.UserInternaInfoRespone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    // Lấy thông tin tài khoản bằng phiên đăng nhập để xác định giỏ hàng.
    @GetMapping("/api/internal/getInfoUser")
    UserInternaInfoRespone getInFo();
}
