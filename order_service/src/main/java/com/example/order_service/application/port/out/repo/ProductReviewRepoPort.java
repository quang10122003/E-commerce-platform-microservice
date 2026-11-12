package com.example.order_service.application.port.out.repo;

import com.example.order_service.application.dto.review.ReviewableOrderItemView;
import com.example.order_service.domain.model.ProductReview;
import com.example.order_service.domain.model.RatingSummary;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProductReviewRepoPort {
    Optional<ReviewableOrderItemView> findReviewableOrderItem(Long orderItemId);

    boolean existsByOrderItemId(Long orderItemId);

    RatingSummary findRatingSummary(Long productId);

    ProductReview save(ProductReview review);

    // Số đánh giá theo từng mức sao của sản phẩm; mức không có đánh giá thì không có trong map.
    Map<Integer, Long> countByRating(Long productId);

    // Trang đánh giá mới nhất kèm ảnh, offset tính từ 0; rating null nghĩa là mọi mức sao.
    List<ProductReview> findProductReviews(Long productId, Integer rating, int offset, int limit);

    int clearDeletedProduct(Long productId);
}
