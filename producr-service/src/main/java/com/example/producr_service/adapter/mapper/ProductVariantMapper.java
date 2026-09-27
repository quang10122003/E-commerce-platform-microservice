package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.AttributeValueEntity;
import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.adapter.entity.VariantImageEntity;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.producr_service.domain.model.VariantImage;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantMapper {

    // Chuyển phân loại persistence cùng thuộc tính và ảnh sang domain model.
    public ProductVariant toDomain(ProductVariantEntity entity) {
        return toDomain(entity, true);
    }

    // Chuyển phân loại cho trang người bán mà không tải ảnh không dùng đến.
    public ProductVariant toSellerDomain(ProductVariantEntity entity) {
        return toDomain(entity, false);
    }

    // Chuyển dữ liệu phân loại theo nhu cầu tải ảnh của từng luồng.
    private ProductVariant toDomain(ProductVariantEntity entity, boolean includeImages) {
        if (entity == null) {
            return null;
        }

        ProductVariant variant = new ProductVariant(
                entity.getId(), entity.getSku(), Money.of(entity.getPrice()),
                entity.getStockQuantity());

        entity.getAttributeValues().stream()
                .map(this::toDomain)
                .forEach(variant::linkAttributeValue);

        if (includeImages) {
            entity.getImages().stream()
                    .map(this::toDomain)
                    .forEach(variant::addImage);
        }

        return variant;
    }

    // Chuyển giá trị thuộc tính persistence sang domain model.
    private AttributeValue toDomain(AttributeValueEntity entity) {
        return new AttributeValue(entity.getId(), entity.getValue());
    }

    // Chuyển ảnh phân loại persistence sang domain model.
    private VariantImage toDomain(VariantImageEntity entity) {
        return new VariantImage(entity.getId(), entity.getImageUrl(), entity.isPrimary());
    }
}
