package com.example.notification.adapter.out.openFeign;

import com.example.notification.adapter.client.ProductFeignClient;
import com.example.notification.application.port.out.client.ProductClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductClientPort {
    private final ProductFeignClient productFeignClient;

    // Chuyển response shop ID thành dữ liệu thuần Java cho application.
    @Override
    public Long getShopId(Long productId) {
        return productFeignClient.getShopId(productId).shopId();
    }

    // Chuyển response tích hợp thành kết quả thuần Java, không che lỗi gọi service.
    @Override
    public boolean existsById(Long productId) {
        return productFeignClient.checkExists(productId).exists();
    }
}
