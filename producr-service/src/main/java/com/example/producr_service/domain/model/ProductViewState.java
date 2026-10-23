package com.example.producr_service.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class ProductViewState {
    // biến định nghĩa khoảng thời gian 1 sự kiện người dungf đc chấp nhận 1 bản ghi
    public static final Duration VIEW_DEDUPLICATION_WINDOW = Duration.ofMinutes(10);

    private final Long userId;
    private final Long productId;
    private final Instant nextAllowedAt;

    public ProductViewState(Long userId, Long productId, Instant nextAllowedAt) {
        this.userId = Objects.requireNonNull(userId);
        this.productId = Objects.requireNonNull(productId);
        this.nextAllowedAt = Objects.requireNonNull(nextAllowedAt);
    }

    // update lần lưu tiếp theo
    public static ProductViewState afterView(Long userId, Long productId, Instant viewedAt) {
        return new ProductViewState(userId, productId, viewedAt.plus(VIEW_DEDUPLICATION_WINDOW));
    }

    public boolean canRecordAt(Instant now) {
        return !now.isBefore(nextAllowedAt);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public Instant getNextAllowedAt() {
        return nextAllowedAt;
    }
}
