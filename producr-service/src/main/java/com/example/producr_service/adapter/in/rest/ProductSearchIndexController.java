package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.ProductSearchIndexSyncResponse;
import com.example.producr_service.application.port.in.SyncProductSearchIndexUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/search-index")
public class ProductSearchIndexController {
    private final SyncProductSearchIndexUseCase syncProductSearchIndexUseCase;

    // Đồng bộ lại product_search và product_embedding từ bảng products, dùng sau khi nạp lại dữ liệu hoặc mất index.
    @PostMapping("/sync")
    public ResponseEntity<ProductSearchIndexSyncResponse> syncAllProducts() {
        return ResponseEntity.ok(syncProductSearchIndexUseCase.syncAllProducts());
    }
}
