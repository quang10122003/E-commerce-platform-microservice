package com.example.order_service.adapter.in.rest;

import com.example.order_service.application.dto.response.ProductReviewResponse;
import com.example.order_service.application.dto.request.CreateReviewRequest;
import com.example.order_service.application.dto.review.ReviewImageUpload;
import com.example.common.response.PageQuery;
import com.example.order_service.application.dto.response.ProductReviewPageResponse;
import com.example.order_service.application.port.in.CreateProductReviewUseCase;
import com.example.order_service.application.port.in.GetProductReviewsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final CreateProductReviewUseCase createProductReviewUseCase;
    private final GetProductReviewsUseCase getProductReviewsUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<ProductReviewResponse> createReview(
            @RequestPart("request") @Valid CreateReviewRequest request,
            @RequestPart(value = "images", required = false) @Size(max = 5) List<MultipartFile> images) throws IOException {
        List<ReviewImageUpload> uploads = new ArrayList<>();
        for (MultipartFile image : images == null ? List.<MultipartFile>of() : images) {
            uploads.add(new ReviewImageUpload(image.getContentType(), image.getBytes()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createProductReviewUseCase.createReview(request, uploads));
    }

    // lấy đánh giá của 1 sản phẩm
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductReviewPageResponse> getProductReviews(
            @PathVariable @Positive Long productId,
            @RequestParam(required = false) @Min(1) @Max(5) Integer rating,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int size) {
        return ResponseEntity.ok(getProductReviewsUseCase.getProductReviews(productId, rating,
                PageQuery.builder().page(page).size(size).build()));
    }
}
