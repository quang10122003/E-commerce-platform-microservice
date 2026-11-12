package com.example.order_service.adapter.out.persistence.review;

import com.example.common.exception.BusinessException;
import com.example.order_service.adapter.mapper.ProductReviewMapper;
import com.example.order_service.adapter.out.persistence.projection.RatingSummaryProjection;
import com.example.order_service.application.dto.review.ReviewableOrderItemView;
import com.example.order_service.application.error.OrderError;
import com.example.order_service.application.port.out.repo.ProductReviewRepoPort;
import com.example.order_service.domain.model.ProductReview;
import com.example.order_service.domain.model.RatingSummary;
import lombok.RequiredArgsConstructor;
import com.example.order_service.adapter.out.persistence.projection.RatingCountProjection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductReviewRepoAdapter implements ProductReviewRepoPort {
    private static final String UNIQUE_ORDER_ITEM = "uk_product_reviews_order_item";

    private final ProductReviewJpa reviews;
    private final ProductReviewMapper mapper;

    @Override
    public Optional<ReviewableOrderItemView> findReviewableOrderItem(Long orderItemId) {
        return reviews.findReviewableOrderItem(orderItemId).map(mapper::toView);
    }

    @Override
    public boolean existsByOrderItemId(Long orderItemId) {
        return reviews.existsByOrderItemId(orderItemId);
    }

    @Override
    public RatingSummary findRatingSummary(Long productId) {
        RatingSummaryProjection summary = reviews.findRatingSummary(productId);
        return new RatingSummary(summary.getReviewCount(), summary.getRatingSum());
    }

    // Flush ngay để hai request đánh giá cùng món nhận lỗi trùng tại đây thay vì lỗi chung lúc commit.
    @Override
    public ProductReview save(ProductReview review) {
        try {
            return mapper.toDomain(reviews.saveAndFlush(mapper.toEntity(review)));
        } catch (DataIntegrityViolationException exception) {
            String message = exception.getMostSpecificCause().getMessage();
            if (message != null && message.contains(UNIQUE_ORDER_ITEM)) {
                throw new BusinessException(OrderError.REVIEW_ALREADY_EXISTS);
            }
            throw exception;
        }
    }

    @Override
    public Map<Integer, Long> countByRating(Long productId) {
        return reviews.countByRating(productId).stream()
                .collect(Collectors.toMap(RatingCountProjection::getRating, RatingCountProjection::getReviewCount));
    }

    // offset luôn là bội của limit vì tính từ số trang nên quy đổi được sang PageRequest.
    @Override
    public List<ProductReview> findProductReviews(Long productId, Integer rating, int offset, int limit) {
        return reviews.findProductReviews(productId, rating, PageRequest.of(offset / limit, limit)).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public int clearDeletedProduct(Long productId) {
        return reviews.clearProductId(productId);
    }
}
