package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.ProductStatus;
import java.math.BigDecimal;
import java.util.List;

public record UserProductDetailResponse(
        Long id, Long shopId, String name, String description, String imageUrl, ProductStatus status,
        String location,
        long totalSold, Long defaultVariantId, List<Attribute> attributes, List<Variant> variants
) {
    public UserProductDetailResponse {
        attributes = List.copyOf(attributes);
        variants = List.copyOf(variants);
    }

    public record Attribute(Long id, String name, List<Value> values) {
        public Attribute {
            values = List.copyOf(values);
        }
    }

    public record Value(Long id, String value) {}

    public record Variant(Long id, BigDecimal price, int stockQuantity, VariantStatus status,
                          List<Long> attributeValueIds, List<Image> images) {
        public Variant {
            attributeValueIds = List.copyOf(attributeValueIds);
            images = List.copyOf(images);
        }
    }

    public record Image(Long id, String imageUrl, boolean primary) {}

    // Trạng thái variant phản ánh tồn kho, không phản ánh quota còn lại của sale.
    public enum VariantStatus {
        AVAILABLE, OUT_OF_STOCK
    }
}
