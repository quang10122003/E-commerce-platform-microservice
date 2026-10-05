package com.example.producr_service.adapter.client;

import com.example.producr_service.application.dto.response.ShopInfoResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    @GetMapping("/api/internal/getInfoUser")
    UserInternaInfoRespone getInfoUser();

    @PostMapping("/api/internal/shop-info")
    List<ShopInfoResponse> getShopInfos(@RequestBody List<Long> shopIds);
}
