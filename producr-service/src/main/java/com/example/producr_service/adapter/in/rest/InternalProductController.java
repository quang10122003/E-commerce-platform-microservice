package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.ProductExistsResponse;
import com.example.producr_service.application.port.in.CheckProductExistsUseCase;
import com.example.producr_service.application.port.in.GetProductShopIdUseCase;
import com.example.producr_service.application.dto.response.ProductShopIdResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/products")
@RequiredArgsConstructor
public class InternalProductController {
    private final CheckProductExistsUseCase checkProductExistsUseCase;
    private final GetProductShopIdUseCase getProductShopIdUseCase;

    // Trả ID shop sở hữu sản phẩm cho service khác.
    @GetMapping("/{productId}/shop-id")
    public ResponseEntity<ProductShopIdResponse> getShopId(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(getProductShopIdUseCase.getShopId(productId));
    }

    // Trả kết quả tồn tại sản phẩm cho service khác, không yêu cầu product ACTIVE.
    @GetMapping("/{productId}/exists")
    public ResponseEntity<ProductExistsResponse> checkExists(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(checkProductExistsUseCase.checkExists(productId));
    }
}
