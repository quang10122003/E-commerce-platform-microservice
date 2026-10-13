package com.example.notification.adapter.client;

import com.example.notification.adapter.client.dto.ProductExistsResponse;
import com.example.notification.adapter.client.dto.ProductShopIdResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "producr-service")
public interface ProductFeignClient {

    // Lấy ID người tạo sản phẩm qua API nội bộ của Product service.
    @GetMapping("/api/internal/products/{productId}/shop-id")
    ProductShopIdResponse getShopId(@PathVariable("productId") Long productId);

    // Gọi trực tiếp API nội bộ qua service discovery, không đi qua gateway.
    @GetMapping("/api/internal/products/{productId}/exists")
    ProductExistsResponse checkExists(@PathVariable("productId") Long productId);
}
