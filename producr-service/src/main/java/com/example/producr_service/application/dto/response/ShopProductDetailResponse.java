package com.example.producr_service.application.dto.response;

import java.math.BigDecimal;
import java.util.List;

// Trả ID thuộc tính, phân loại và ảnh để form giữ đúng các bản ghi khi lưu lại.
public record ShopProductDetailResponse(
        Long id, Long categoryId, Long brandId, String name, String description,
        String imageUrl, List<Attribute> attributes, List<Variant> variants
) {
    public record Attribute(Long id, String name, List<Value> values) {
    }

    public record Value(Long id, String value) {
    }

    public record Variant(Long id, String sku, BigDecimal price, int stockQuantity,
                          List<Selection> attributeSelections, List<Image> images) {
    }

    public record Selection(int attributeIndex, int valueIndex) {
    }

    public record Image(Long id, String imageUrl, boolean primary) {
    }
}
