package com.example.notification.adapter.out.openFeign;

import com.example.notification.adapter.client.ProductFeignClient;
import com.example.notification.application.dto.response.ChatProductInfo;
import com.example.notification.application.port.out.client.ProductClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductClientPort {
    private final ProductFeignClient productFeignClient;

    @Override
    public List<ChatProductInfo> findProductInfosByIds(List<Long> productIds) {
        return productIds.isEmpty() ? List.of() : productFeignClient.getProductSummariesByIds(productIds);
    }

    @Override
    public Long getShopId(Long productId) {
        return productFeignClient.getShopId(productId).shopId();
    }

    @Override
    public boolean existsById(Long productId) {
        return productFeignClient.checkExists(productId).exists();
    }
}
