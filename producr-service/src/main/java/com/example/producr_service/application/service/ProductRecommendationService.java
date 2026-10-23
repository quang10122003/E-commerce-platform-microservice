package com.example.producr_service.application.service;

import com.example.common.exception.BusinessException;
import com.example.producr_service.application.dto.request.RecordProductViewRequest;
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
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class ProductRecommendationService implements RecordProductViewUseCase {
    private final CurrentUserPort currentUserPort;
    private final ProductRepositoryPort productRepositoryPort;
    private final ProductBehaviorRepositoryPort productBehaviorRepositoryPort;
    private final ProductViewStateRepositoryPort productViewStateRepositoryPort;
    private final Clock clock;
    private final ProductVariantRepoPort productVariantRepoPort;

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
}
