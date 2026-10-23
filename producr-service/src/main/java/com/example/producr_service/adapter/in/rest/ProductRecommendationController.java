package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.request.RecordProductViewRequest;
import com.example.producr_service.application.port.in.RecordProductViewUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products/recommendations")
@RequiredArgsConstructor
// các api đánh sự kiện người dùng cx như như các api liên quan đến chức năng recommnent product
public class ProductRecommendationController {
    private final RecordProductViewUseCase recordProductViewUseCase;

    @PostMapping("/behaviors/views/{productId}")
    public ResponseEntity<Void> recordProductView(
            @PathVariable("productId") @Positive Long productId,
            @RequestBody @Valid RecordProductViewRequest request) {
        recordProductViewUseCase.recordProductView(productId, request);
        return ResponseEntity.noContent().build();
    }
}
