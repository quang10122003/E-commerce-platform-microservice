package com.example.producr_service.application.dto.request;

import java.math.BigDecimal;
import java.util.List;

// Giữ thông tin sản phẩm và phân loại từ form để đối chiếu với dữ liệu đã lưu.
public record UpdateProductData(
        Long categoryId,
        Long brandId,
        String name,
        String description,
        List<Attribute> attributes,
        List<Variant> variants
) {
    public record Attribute(Long id, String name, List<Value> values) {}
    public record Value(Long id, String value) {}
    public record Variant(Long id, BigDecimal price, int stockQuantity,
                          List<Selection> attributeSelections, List<Image> images) {}
    public record Selection(Integer attributeIndex, Integer valueIndex) {}
    public record Image(Long id, boolean primary) {}
}
