package com.example.order_service.application.port.in;

import com.example.common.response.PageQuery;
import com.example.order_service.application.dto.response.ProductReviewPageResponse;

public interface GetProductReviewsUseCase {
    // rating null nghĩa là mọi mức sao; tổng quan luôn tính trên toàn bộ đánh giá.
    ProductReviewPageResponse getProductReviews(Long productId, Integer rating, PageQuery pageQuery);
}
