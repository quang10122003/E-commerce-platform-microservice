package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.order_service.domain.error.ReviewDomainError;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Đánh giá của người mua cho một món trong đơn; phân loại sản phẩm lấy từ snapshot của order_items.
public final class ProductReview {
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int MAX_IMAGES = 5;
    public static final int MAX_CONTENT_LENGTH = 2000;
    public static final Duration REVIEW_PERIOD = Duration.ofDays(7);

    private final Long id;
    private final Long orderItemId;
    private final Long orderId;
    private final Long productId;
    private final Long shopId;
    private final Long userId;
    private final String reviewerNameSnapshot;
    private final String variantAttributes;
    private final int rating;
    private final String content;
    private final List<ReviewImage> images;
    private final LocalDateTime createdAt;

    // Kiểm tra dữ liệu bắt buộc, số sao 1–5 và độ dài nội dung trước khi tạo đối tượng đánh giá.
    private ProductReview(Long id, Long orderItemId, Long orderId, Long productId, Long shopId, Long userId,
                          String reviewerNameSnapshot, String variantAttributes, int rating, String content,
                          List<ReviewImage> images, LocalDateTime createdAt) {
        if (orderItemId == null || orderId == null || reviewerNameSnapshot == null || reviewerNameSnapshot.isBlank()
                || variantAttributes == null || createdAt == null) {
            throw new BusinessException(ReviewDomainError.INVALID_DATA);
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(ReviewDomainError.INVALID_RATING);
        }
        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ReviewDomainError.CONTENT_TOO_LONG);
        }
        this.id = id;
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.shopId = shopId;
        this.userId = userId;
        // Snapshot tên người mua có thể dài hơn cột; cắt để không làm hỏng đánh giá.
        this.reviewerNameSnapshot = reviewerNameSnapshot.length() > 255
                ? reviewerNameSnapshot.substring(0, 255) : reviewerNameSnapshot;
        this.variantAttributes = variantAttributes;
        this.rating = rating;
        this.content = content;
        this.images = new ArrayList<>(images);
        this.createdAt = createdAt;
    }

    // Tạo đánh giá mới: chỉ cho đơn đã giao thành công, trong 7 ngày kể từ lúc giao, tối đa 5 ảnh; nội dung rỗng lưu là null.
    public static ProductReview create(Long orderItemId, Long orderId, Long productId, Long shopId, Long userId,
                                       String reviewerName, String variantAttributes, OrderStatus orderStatus,
                                       LocalDateTime deliveredAt, int rating, String content, int imageCount,
                                       LocalDateTime now) {
        if (orderStatus != OrderStatus.DONE || deliveredAt == null) {
            throw new BusinessException(ReviewDomainError.ORDER_NOT_COMPLETED);
        }
        if (!isWithinReviewPeriod(orderStatus, deliveredAt, now)) {
            throw new BusinessException(ReviewDomainError.REVIEW_PERIOD_EXPIRED);
        }
        if (imageCount < 0 || imageCount > MAX_IMAGES) {
            throw new BusinessException(ReviewDomainError.TOO_MANY_IMAGES);
        }
        String normalizedContent = content == null || content.isBlank() ? null : content.trim();
        return new ProductReview(null, orderItemId, orderId, productId, shopId, userId, reviewerName,
                variantAttributes, rating, normalizedContent, List.of(), now);
    }

    // Đơn còn trong hạn đánh giá: đã giao thành công và chưa quá 7 ngày kể từ lúc giao (tính cả đúng mốc 7 ngày).
    public static boolean isWithinReviewPeriod(OrderStatus orderStatus, LocalDateTime deliveredAt, LocalDateTime now) {
        return orderStatus == OrderStatus.DONE && deliveredAt != null && !now.isAfter(deliveredAt.plus(REVIEW_PERIOD));
    }

    // Dựng lại đánh giá đã lưu trong DB (bắt buộc có id), không kiểm tra lại hạn đánh giá.
    public static ProductReview reconstitute(Long id, Long orderItemId, Long orderId, Long productId, Long shopId,
                                             Long userId, String reviewerNameSnapshot, String variantAttributes,
                                             int rating, String content, List<ReviewImage> images,
                                             LocalDateTime createdAt) {
        if (id == null) {
            throw new BusinessException(ReviewDomainError.INVALID_DATA);
        }
        return new ProductReview(id, orderItemId, orderId, productId, shopId, userId, reviewerNameSnapshot,
                variantAttributes, rating, content, images, createdAt);
    }

    // Gắn ảnh đã upload vào đánh giá theo đúng thứ tự, không vượt quá 5 ảnh.
    public void addImage(ReviewImage image) {
        if (images.size() >= MAX_IMAGES) {
            throw new BusinessException(ReviewDomainError.TOO_MANY_IMAGES);
        }
        if (image.getSortOrder() != images.size()) {
            throw new BusinessException(ReviewDomainError.INVALID_DATA);
        }
        images.add(image);
    }

    public Long getId() {
        return id;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getShopId() {
        return shopId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getReviewerNameSnapshot() {
        return reviewerNameSnapshot;
    }

    public String getVariantAttributes() {
        return variantAttributes;
    }

    public int getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public List<ReviewImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
