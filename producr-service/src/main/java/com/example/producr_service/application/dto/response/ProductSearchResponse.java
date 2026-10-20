package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;

import java.math.BigDecimal;

public record ProductSearchResponse(Long id,
                                    String name,
                                    String location,
                                    Long UserId,
                                    String description,
                                    Long categoryId,
                                    String categoryName,
                                    Long brandId,
                                    String brandName,
                                    ProductStatus status,
                                    String imageUrl,
                                    BigDecimal maxPrice,
                                    long totalSold,
                                    BigDecimal maxDiscountPercent) {


    public ProductSearchResponse withMaxDiscountPercent(BigDecimal discountPercent) {
        return new ProductSearchResponse(id, name, location, UserId, description,
                categoryId, categoryName, brandId, brandName, status, imageUrl,
                maxPrice, totalSold, discountPercent);
    }
}
