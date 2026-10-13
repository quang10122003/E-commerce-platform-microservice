package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;
import java.math.BigDecimal;

// Dữ liệu tối thiểu để kiểm tra giá và tồn kho của variant thuộc đúng product.
public record VariantPriceView(Long variantId, BigDecimal price, int stockQuantity, ProductStatus productStatus) {}
