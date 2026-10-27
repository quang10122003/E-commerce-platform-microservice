package com.example.producr_service.adapter.in.rest;

import com.example.common.response.PageResponse;
import com.example.common.response.PageQuery;
import com.example.producr_service.application.dto.request.RecordProductViewRequest;
import com.example.producr_service.application.port.in.GetRecommendedProductsUseCase;
import com.example.producr_service.application.port.in.RecordProductViewUseCase;
import com.example.producr_service.application.port.in.GetTopSellingProductsUseCase;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/recommendations")
@RequiredArgsConstructor
// các api đánh sự kiện người dùng cx như như các api liên quan đến chức năng recommnent product
public class ProductRecommendationController {
    private final RecordProductViewUseCase recordProductViewUseCase;
    private final GetTopSellingProductsUseCase getTopSellingProductsUseCase;
    private final GetRecommendedProductsUseCase getRecommendedProductsUseCase;

    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductSearchResponse>> getTopSellingProducts() {
        return ResponseEntity.ok(getTopSellingProductsUseCase.getTopSellingProducts());
    }

    // đánh dấu thao tác xem sản phẩm user
    @PostMapping("/behaviors/views/{productId}")
    public ResponseEntity<Void> recordProductView(
            @PathVariable("productId") @Positive Long productId,
            @RequestBody @Valid RecordProductViewRequest request) {
        recordProductViewUseCase.recordProductView(productId, request);
        return ResponseEntity.noContent().build();
    }

    // lấy sản phẩm theo gợi ý sở thích
    @GetMapping
    public ResponseEntity<PageResponse<ProductSearchResponse>> getRecommendedProducts(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {

        return ResponseEntity.ok(
                getRecommendedProductsUseCase.getRecommendedProducts(
                        new PageQuery(page, size)));
    }
}
