package com.example.notification.adapter.client;

import com.example.notification.adapter.client.dto.ProductExistsResponse;
import com.example.notification.adapter.client.dto.ProductShopIdResponse;
import com.example.notification.application.dto.response.ChatProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// API nội bộ đi qua service discovery để không phụ thuộc định tuyến của Gateway.
@FeignClient(name = "producr-service")
public interface ProductFeignClient {
    @PostMapping("/api/internal/products/summaries")
    List<ChatProductInfo> getProductSummariesByIds(
            @RequestBody List<Long> productIds);

    @GetMapping("/api/internal/products/{productId}/shop-id")
    ProductShopIdResponse getShopId(@PathVariable("productId") Long productId);

    @GetMapping("/api/internal/products/{productId}/exists")
    ProductExistsResponse checkExists(@PathVariable("productId") Long productId);
}
