package com.example.order_service.application.port.in;

import com.example.order_service.application.dto.response.ProductReviewResponse;
import com.example.order_service.application.dto.request.CreateReviewRequest;
import com.example.order_service.application.dto.review.ReviewImageUpload;

import java.util.List;

public interface CreateProductReviewUseCase {
    ProductReviewResponse createReview(CreateReviewRequest request, List<ReviewImageUpload> images);
}
