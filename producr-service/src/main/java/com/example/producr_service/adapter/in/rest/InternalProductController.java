package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.ProductExistsResponse;
import com.example.producr_service.application.dto.response.ProductShopIdResponse;
import com.example.producr_service.application.dto.response.ProductSummaryResponse;
import com.example.producr_service.application.dto.response.CartInfoRepone;
import com.example.producr_service.application.port.in.CheckProductExistsUseCase;
import com.example.producr_service.application.port.in.GetProductShopIdUseCase;
import com.example.producr_service.application.port.in.GetProductSummariesUseCase;
import com.example.producr_service.application.port.in.GetCartInfoUseCase;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalProductController {
    private final CheckProductExistsUseCase checkProductExistsUseCase;
    private final GetProductShopIdUseCase getProductShopIdUseCase;
    private final GetProductSummariesUseCase getProductSummariesUseCase;
    private final GetCartInfoUseCase getCartInfoUseCase;

    @PostMapping("/cart-info")
    public ResponseEntity<List<CartInfoRepone>> getCartInfo(
            @RequestBody @NotEmpty(message = "Danh sách variant không được để trống")
            List<@NotNull(message = "ID variant không được để trống") Long> variantIds) {
        return ResponseEntity.ok(getCartInfoUseCase.getCartInfo(variantIds));
    }

    // Chat vẫn cần thông tin hiển thị của sản phẩm ngừng bán nên không lọc ACTIVE.
    @PostMapping("/products/summaries")
    public List<ProductSummaryResponse> getProductSummariesByIds(
            @RequestBody @Size(max = 50) List<@NotNull @Positive Long> productIds) {
        return getProductSummariesUseCase.getProductSummariesByIds(productIds);
    }

    @GetMapping("/products/{productId}/shop-id")
    public ResponseEntity<ProductShopIdResponse> getShopId(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(getProductShopIdUseCase.getShopId(productId));
    }

    // Ngừng bán không đồng nghĩa đã xóa, nên kiểm tra tồn tại không lọc ACTIVE.
    @GetMapping("/products/{productId}/exists")
    public ResponseEntity<ProductExistsResponse> checkExists(
            @PathVariable("productId") Long productId
    ) {
        return ResponseEntity.ok(checkProductExistsUseCase.checkExists(productId));
    }
}
