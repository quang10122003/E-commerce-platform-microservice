package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CacheAccessException;
import com.example.common.response.PageResponse;
import com.example.common.response.PageQuery;
import com.example.producr_service.application.dto.embedding.ProductEmbeddingData;
import com.example.producr_service.application.dto.embedding.ProductPreferenceData;
import com.example.producr_service.application.dto.embedding.UserPreferenceProfile;
import com.example.producr_service.application.dto.request.RecordProductViewRequest;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.constant.ProductCacheKeys;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.port.in.GetRecommendedProductsUseCase;
import com.example.producr_service.application.port.out.cache.CachePort;
import com.example.producr_service.application.port.out.embedding.ProductEmbeddingRepositoryPort;
import com.example.producr_service.application.dto.event.CartItemAddedEvent;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.in.RecordProductViewUseCase;
import com.example.producr_service.application.port.out.client.CurrentUserPort;
import com.example.producr_service.application.port.out.repo.ProductBehaviorRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductViewStateRepositoryPort;
import com.example.producr_service.domain.model.BehaviorType;
import com.example.producr_service.domain.model.ProductViewState;
import com.example.producr_service.domain.model.UserProductBehavior;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class ProductRecommendationService implements RecordProductViewUseCase, GetRecommendedProductsUseCase {
    private final CurrentUserPort currentUserPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductBehaviorRepositoryPort productBehaviorRepositoryPort;
    private final ProductViewStateRepositoryPort productViewStateRepositoryPort;
    private final Clock clock;
    private final ProductVariantRepoPort productVariantRepoPort;
    private final ProductEmbeddingRepositoryPort productEmbeddingRepositoryPort;
    private final CachePort cachePort;
    private final UserPreferenceCalculator userPreferenceCalculator;
    private final ProductService productService;

    @Transactional
    public void recordAddToCart(CartItemAddedEvent event) {
        Instant createdAt = event.occurredAt().toInstant();
        if (!createdAt.isAfter(clock.instant().minus(UserProductBehavior.RETENTION))) {
            log.info("Bỏ qua hành vi hết hạn: event_id={}, event_type={}", event.eventId(), event.eventType());
            return;
        }
        Optional<Long> productId = productVariantRepoPort.findProductIdByVariantId(event.productVariantId());
        if (productId.isEmpty()) {
            log.warn("Bỏ qua hành vi không tìm thấy variant/product: event_id={}, event_type={}, variant_id={}",
                    event.eventId(), event.eventType(), event.productVariantId());
            return;
        }
        boolean inserted = productBehaviorRepositoryPort.saveIfAbsent(new UserProductBehavior(
                event.eventId().toString(), event.userId(), productId.get(), BehaviorType.ADD_TO_CART, createdAt));
        log.info("Ghi nhận hành vi thêm giỏ: event_id={}, event_type={}, product_id={}, result={}",
                event.eventId(), event.eventType(), productId.get(), inserted ? "INSERTED" : "DUPLICATE");
    }

    @Override
    @Transactional
    public void recordProductView(Long productId, RecordProductViewRequest request) {
        Long userId = currentUserPort.getUserInfo().userId();
        if (!productRepositoryPort.existsById(productId)) {
            throw new BusinessException(ProductError.PRODUCT_NOT_FOUND);
        }

        ProductViewState viewState = productViewStateRepositoryPort.lockOrCreate(userId, productId, clock.instant());
        //  check xem sự kiện xem này đã đc phép ghi trong khoảng thời gian quy định chưa
        Instant now = clock.instant();
        if (!viewState.canRecordAt(now)) {
            return;
        }

        Optional<Instant> latestViewAt = productBehaviorRepositoryPort.findLatestProductViewAt(userId, productId);

        if (latestViewAt.isPresent()) {
            ProductViewState latestState = ProductViewState.afterView(userId, productId, latestViewAt.get());
            if (!latestState.canRecordAt(now)) {
                productViewStateRepositoryPort.save(latestState);
                return;
            }
        }
        boolean inserted = productBehaviorRepositoryPort.saveIfAbsent(new UserProductBehavior(
                request.eventId(), userId, productId, BehaviorType.PRODUCT_VIEW, now));
        if (inserted) {
            productViewStateRepositoryPort.save(ProductViewState.afterView(userId, productId, now));
        }
    }

    @Override
    public PageResponse<ProductSearchResponse> getRecommendedProducts(PageQuery pageQuery) {
        int pageSize = pageQuery.size();
        Long userId = currentUserPort.getUserInfo().userId();
        List<Long> validIds = filterEligibleIds(userId, getOrBuildIds(userId), clock.instant());

        int totalItems = validIds.size();

        long offset = (long) pageQuery.pageIndex() * pageSize;

        List<Long> pageIds = offset >= totalItems ? List.of() : validIds.subList(
                (int) offset, (int) Math.min((long) totalItems, offset + pageSize));

        List<ProductSearchResponse> items = loadPageItems(pageIds);
        int totalPages = (int) ((totalItems + (long) pageSize - 1) / pageSize);
        return new PageResponse<>(items, pageQuery.page(), pageSize, totalItems, totalPages);
    }

    // tạo repone
    private List<ProductSearchResponse> loadPageItems(List<Long> pageIds) {
        if (pageIds.isEmpty()) {
            return List.of();
        }
        Map<Long, ProductSearchResponse> productsById = productService.getAvailableSearchItemsByIds(pageIds).stream()
                .collect(Collectors.toMap(ProductSearchResponse::id, Function.identity()));
        return pageIds.stream().map(productsById::get).filter(Objects::nonNull).toList();
    }

    // lấy danh sách id của sản phẩm recommnt từ cache or tạo nếu hết cache
    private List<Long> getOrBuildIds(Long userId) {
        String key = ProductCacheKeys.recommendations(userId);
        try {
            Optional<List<Long>> cached = findCachedRecommendationIds(key);
            if (cached.isPresent()) {
                return cached.get();
            }
            List<Long> ids = buildRecommendedIds(userId, clock.instant());
            cachePort.setIfAbsent(key, ids, Constant.RECOMMENDATION_CACHE_TTL);
            // tranh 2 request đồng thời tạo cache nên là khi lưu xong lấy data từ cache
            return findCachedRecommendationIds(key).orElseThrow(() ->
                    new BusinessException(ProductError.RECOMMENDATION_CACHE_UNAVAILABLE));
        } catch (CacheAccessException exception) {
            log.warn("Không thể đọc hoặc ghi cache gợi ý");
            throw new BusinessException(ProductError.RECOMMENDATION_CACHE_UNAVAILABLE, exception);
        }
    }

    private Optional<List<Long>> findCachedRecommendationIds(String key) {
        Optional<List<Long>> cached = cachePort.getList(key, Long.class);
        if (cached.isPresent() && cached.get().stream().anyMatch(id -> id <= 0)) {
            throw new BusinessException(ProductError.RECOMMENDATION_CACHE_UNAVAILABLE);
        }
        return cached;
    }

    // tạo list id product cho recomment
    private List<Long> buildRecommendedIds(Long userId, Instant now) {
        // lấy hành vi người dùng
        List<UserProductBehavior> behaviors = productBehaviorRepositoryPort.findRecentByUserId(userId, now);
        // lấy list id mà hành vi người dùng tương tác
        List<Long> historyIds = behaviors.stream().map(UserProductBehavior::getProductId).distinct().toList();
        // lấy thông tin các sản phẩm đc lấy từ hành vi người dùng
        Map<Long, ProductPreferenceData> products = productRepositoryPort.findPreferenceDataByIds(historyIds).stream()
                .collect(Collectors.toMap(ProductPreferenceData::productId, Function.identity()));

        UserPreferenceProfile profile = userPreferenceCalculator.buildProfile(behaviors, products,
                productEmbeddingRepositoryPort.findAllByProductIds(historyIds), now);

        List<Long> topIds = productService.getTopSellingProducts().stream()
                .map(ProductSearchResponse::id).toList();

        List<Long> recommendations;
        // nếu mà dữ liện người dùng trống thì lấy top sale
        if (!profile.hasSignals()) {
            recommendations = filterEligibleIds(userId, topIds, now).stream()
                    .limit(Constant.RECOMMENDATION_CACHE_LIMIT).toList();
        } else {
            List<Long> candidates = collectCandidateIds(profile, topIds);
            List<Long> eligibleIds = filterEligibleIds(userId, candidates, now);
//            lấy dữ liệu của các sản phẩm ứng viên rồi chấm điểm, sắp xếp để tạo danh sách gợi ý
            recommendations = userPreferenceCalculator.rank(profile,
                    productRepositoryPort.findAvailableSearchItemsByIds(eligibleIds),
                    productEmbeddingRepositoryPort.findAllByProductIds(eligibleIds));
        }
        log.info("Tạo danh sách gợi ý: user_id={}, behavior_count={}, recommendation_count={}, personalized={}",
                userId, behaviors.size(), recommendations.size(), profile.hasSignals());
        return recommendations;
    }
//    Hàm này gom các sản phẩm ứng viên để đem đi lọc và chấm điểm
    private List<Long> collectCandidateIds(UserPreferenceProfile profile, List<Long> topIds) {
        Set<Long> ids = new LinkedHashSet<>();
        if (!profile.vector().isEmpty()) {
            // lấy các sản phẩm có vector giống user vector
            ids.addAll(productEmbeddingRepositoryPort.findSimilarProductIds(
                    profile.vector(), Constant.RECOMMENDATION_VECTOR_CANDIDATE_LIMIT));
        }
        // thêm các sản phẩm thuộc danh mục mà user quan tâm vào tập
        ids.addAll(productRepositoryPort.findCategoryCandidateIds(
                profile.categoryScores().keySet(), Constant.RECOMMENDATION_RULE_CANDIDATE_LIMIT));
//        thêm các sản phẩm thuộc brand mà user quan tâm vào tập
        ids.addAll(productRepositoryPort.findBrandCandidateIds(
                profile.brandScores().keySet(), Constant.RECOMMENDATION_RULE_CANDIDATE_LIMIT));
        // thêm cả top sale vào tập
        ids.addAll(topIds);
        return new ArrayList<>(ids);
    }
//    lọc danh sách ID để chỉ giữ sản phẩm được phép gợi ý
    private List<Long> filterEligibleIds(Long userId, List<Long> productIds, Instant now) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        Set<Long> availableIds = productRepositoryPort.findAvailableProductIds(productIds);
        Set<Long> purchasedIds = findPurchasedProductIdsInLastSevenDaysDemo(userId, now);
        return productIds.stream().distinct().filter(availableIds::contains)
                .filter(id -> !purchasedIds.contains(id)).toList();
    }

    private Set<Long> findPurchasedProductIdsInLastSevenDaysDemo(Long userId, Instant now) {
        // TODO: Thay bằng dữ liệu đơn hoàn thành khi triển khai chức năng mua hàng.
        return Set.of();
    }
}
