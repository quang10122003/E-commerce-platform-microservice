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

// Gọi trực tiếp API nội bộ qua service discovery, không đi qua gateway.
@FeignClient(name = "producr-service")
public interface ProductFeignClient {
    // Lấy thông tin sản phẩm theo danh sách ID, kể cả sản phẩm ngừng bán.
    @PostMapping("/api/internal/products/summaries")
    List<ChatProductInfo> getProductSummariesByIds(
            @RequestBody List<Long> productIds);

    // Xác định shop sở hữu sản phẩm; báo lỗi nếu sản phẩm không tồn tại.
    @GetMapping("/api/internal/products/{productId}/shop-id")
    ProductShopIdResponse getShopId(@PathVariable("productId") Long productId);

    // Kiểm tra sản phẩm còn tồn tại, không phụ thuộc trạng thái bán.
    @GetMapping("/api/internal/products/{productId}/exists")
    ProductExistsResponse checkExists(@PathVariable("productId") Long productId);
}
