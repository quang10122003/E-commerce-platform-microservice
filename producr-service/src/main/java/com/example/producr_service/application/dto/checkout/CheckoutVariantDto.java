package com.example.producr_service.application.dto.checkout;

import com.example.producr_service.domain.model.ProductStatus;
import com.example.producr_service.domain.model.ProductVariant;

public record CheckoutVariantDto(Long productId, Long shopId, String productName, ProductStatus productStatus,
                                 String variantAttributes, String imageUrl, ProductVariant variant) {
}
