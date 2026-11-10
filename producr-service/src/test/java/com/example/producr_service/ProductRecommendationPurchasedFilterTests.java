package com.example.producr_service;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.dto.response.UserInternaInfoRespone;
import com.example.producr_service.application.port.out.cache.CachePort;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.repo.ProductViewStateRepositoryPort;
import com.example.producr_service.application.service.ProductRecommendationService;
import com.example.producr_service.application.service.ProductService;
import com.example.producr_service.application.service.UserPreferenceCalculator;
import com.example.producr_service.domain.model.BehaviorType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRecommendationPurchasedFilterTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-09T05:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));
    private final CurrentUserPort currentUser = mock(CurrentUserPort.class);
    private final ProductRepositoryPort products = mock(ProductRepositoryPort.class);
    private final ProductBehaviorRepositoryPort behaviors = mock(ProductBehaviorRepositoryPort.class);
    private final CachePort cache = mock(CachePort.class);
    private final ProductService productService = mock(ProductService.class);
    private final ProductRecommendationService service = new ProductRecommendationService(currentUser, products,
            behaviors, mock(ProductViewStateRepositoryPort.class), clock, mock(ProductVariantRepoPort.class),
            mock(ProductEmbeddingRepositoryPort.class), cache, mock(UserPreferenceCalculator.class), productService);

    private ProductSearchResponse item(long id) {
        ProductSearchResponse response = mock(ProductSearchResponse.class);
        when(response.id()).thenReturn(id);
        return response;
    }

    @Test
    void recommendationsExcludeProductsPurchasedInLastSevenDays() {
        when(currentUser.getUserInfo()).thenReturn(UserInternaInfoRespone.builder().userId(2L).build());
        when(cache.getList(anyString(), eq(Long.class))).thenReturn(Optional.of(List.of(1L, 2L, 3L)));
        when(products.findAvailableProductIds(any())).thenReturn(Set.of(1L, 2L, 3L));
        when(behaviors.findProductIdsByUserIdAndTypeSince(eq(2L), eq(BehaviorType.PURCHASE), any()))
                .thenReturn(Set.of(2L));
        List<ProductSearchResponse> remaining = List.of(item(1L), item(3L));
        when(products.findAvailableSearchItemsByIds(List.of(1L, 3L))).thenReturn(remaining);
        when(productService.filterAvailableShopProducts(remaining)).thenReturn(remaining);
        when(productService.getAvailableSearchItemsByIds(List.of(1L, 3L))).thenReturn(remaining);

        PageResponse<ProductSearchResponse> page = service.getRecommendedProducts(new PageQuery(1, 10));

        assertEquals(List.of(1L, 3L), page.items().stream().map(ProductSearchResponse::id).toList());
        assertEquals(2, page.totalItems());
        verify(behaviors).findProductIdsByUserIdAndTypeSince(2L, BehaviorType.PURCHASE,
                clock.instant().minus(Constant.RECOMMENDATION_PURCHASED_EXCLUDE_DURATION));
    }

    @Test
    void recommendationsKeepEverythingWhenNothingPurchased() {
        when(currentUser.getUserInfo()).thenReturn(UserInternaInfoRespone.builder().userId(2L).build());
        when(cache.getList(anyString(), eq(Long.class))).thenReturn(Optional.of(List.of(1L, 2L)));
        when(products.findAvailableProductIds(any())).thenReturn(Set.of(1L, 2L));
        when(behaviors.findProductIdsByUserIdAndTypeSince(eq(2L), eq(BehaviorType.PURCHASE), any()))
                .thenReturn(Set.of());
        List<ProductSearchResponse> all = List.of(item(1L), item(2L));
        when(products.findAvailableSearchItemsByIds(List.of(1L, 2L))).thenReturn(all);
        when(productService.filterAvailableShopProducts(all)).thenReturn(all);
        when(productService.getAvailableSearchItemsByIds(List.of(1L, 2L))).thenReturn(all);

        PageResponse<ProductSearchResponse> page = service.getRecommendedProducts(new PageQuery(1, 10));

        assertEquals(2, page.totalItems());
    }
}
