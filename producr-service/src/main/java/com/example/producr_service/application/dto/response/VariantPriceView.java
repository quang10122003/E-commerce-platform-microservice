package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;
import java.math.BigDecimal;

public record VariantPriceView(Long variantId, BigDecimal price, int stockQuantity, ProductStatus productStatus) {}
