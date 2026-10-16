package com.example.producr_service.adapter.client;

import com.example.producr_service.application.dto.response.ShopInfoResponse;
import com.example.producr_service.adapter.client.dto.PublicShopInfoResponse;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service")
public interface AuthUserFeignClient {
    // Lấy thông tin công khai của shop theo ID, không yêu cầu đăng nhập.
    @GetMapping("/api/internal/shops/{shopId}/info")
    PublicShopInfoResponse getPublicShopInfo(@PathVariable("shopId") Long shopId);

    // Lấy thông tin shop bằng phiên đăng nhập hiện tại.
    @GetMapping("/api/internal/getInfoUser")
    UserInternaInfoRespone getInfoUser();

    // Lấy thông tin các shop theo danh sách ID để ghép với sản phẩm.
    @PostMapping("/api/internal/shop-info")
    List<ShopInfoResponse> getShopInfos(@RequestBody List<Long> shopIds);
}
