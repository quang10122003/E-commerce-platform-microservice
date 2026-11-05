package com.example.producr_service.application.service;

import java.util.List;

import com.example.common.exception.BusinessException;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.request.ShopProductFilter;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.port.in.GetShopProductsUseCase;
import com.example.producr_service.application.dto.response.ShopOverviewResponse;
import com.example.producr_service.application.dto.response.ShopProfileResponse;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.GetShopOverviewUseCase;
import com.example.producr_service.application.port.out.client.ShopInfoClientPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
public class ShopOverviewService implements GetShopOverviewUseCase, GetShopProductsUseCase {
    private final ShopInfoClientPort shopInfoClientPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final Clock clock;
    private final ProductService productService;
    private final ShopAccessService shopAccessService;
    private final ProductHelper productHelper;

    @Override
    public PageResponse<ProductSearchResponse> getShopProducts(Long shopId, ShopProductFilter filter) {
        shopAccessService.requireAvailableShops(List.of(shopId));
        return productService.getShopProducts(shopId, filter);
    }

    @Override
    public ShopOverviewResponse getShopOverviewByProductId(Long productId) {
        Long shopId = productRepositoryPort.findOwnerIdById(productId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_NOT_FOUND));
        ShopProfileResponse shop = shopInfoClientPort.findPublicShopInfo(shopId)
                .orElseThrow(() -> new BusinessException(ProductError.SHOP_NOT_FOUND));
        long productCount = productRepositoryPort.countActiveByUserId(shopId);
        OffsetDateTime shopCreatedAt = shop.shopCreatedAt() == null ? null
                : shop.shopCreatedAt().atZone(clock.getZone()).toOffsetDateTime();
        return new ShopOverviewResponse(shop.shopId(), shop.shopName(),
                productHelper.extractProvinceNameFromAddress(shop.location()), shop.shopLogoUrl(),
                shop.shopDescription(), productCount, shopCreatedAt);
    }
}
