package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.ShopOverviewResponse;
import com.example.producr_service.application.port.in.GetShopOverviewUseCase;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.ShopProductFilter;
import com.example.producr_service.application.dto.request.ShopProductSortOption;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.port.in.GetShopProductsUseCase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import com.example.producr_service.application.port.in.GetShopTopProductsUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ShopOverviewController {
    private final GetShopOverviewUseCase getShopOverviewUseCase;
    private final GetShopProductsUseCase getShopProductsUseCase;
    private final GetShopTopProductsUseCase getShopTopProductsUseCase;

    @GetMapping("/shops/{shopId}/products")
    public ResponseEntity<PageResponse<ProductSearchResponse>> getShopProducts(
            @PathVariable("shopId") @Positive Long shopId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "RELEVANCE", name = "sort") ShopProductSortOption sort) {
        ShopProductFilter filter = new ShopProductFilter(new PageQuery(page, size), keyword, categoryId, sort);
        return ResponseEntity.ok(getShopProductsUseCase.getShopProducts(shopId, filter));
    }

    // 10 sản phẩm nổi bật của shop cho trang shop công khai.
    @GetMapping("/shops/{shopId}/top-products")
    public ResponseEntity<List<ProductSearchResponse>> getShopTopProducts(@PathVariable("shopId") @Positive Long shopId) {
        return ResponseEntity.ok(getShopTopProductsUseCase.getShopTopProducts(shopId));
    }

    // Trang shop công khai chỉ tính product ACTIVE, không tính variant.
    @GetMapping("/{productId}/shop/overview")
    public ResponseEntity<ShopOverviewResponse> getShopOverview(@PathVariable("productId") @Positive Long productId) {
        return ResponseEntity.ok(getShopOverviewUseCase.getShopOverviewByProductId(productId));
    }
}
