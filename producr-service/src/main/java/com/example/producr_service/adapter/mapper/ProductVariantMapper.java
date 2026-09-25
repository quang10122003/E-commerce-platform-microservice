package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.AttributeValueEntity;
import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.adapter.entity.VariantImageEntity;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.producr_service.domain.model.VariantImage;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProductVariantMapper {

    // Chuyển phân loại persistence cùng thuộc tính và ảnh sang domain model.
    public ProductVariant toDomain(ProductVariantEntity entity) {
        if (entity == null) {
            return null;
        }

        ProductVariant variant = new ProductVariant(
                entity.getId(),
                entity.getSku(),
                Money.of(entity.getPrice()),
                entity.getStockQuantity()
        );

        entity.getAttributeValues().stream()
                .map(this::toDomain)
                .forEach(variant::linkAttributeValue);
        entity.getImages().stream()
                .map(this::toDomain)
                .forEach(variant::addImage);

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

    // Chuyển entity cùng các giá trị thuộc tính thành dữ liệu phân loại cho response người bán.
    public SellerProductItemResponse.Variant toSellerVariant(ProductVariantEntity variant) {
        List<SellerProductItemResponse.VariantAttribute> attributes = variant.getAttributeValues()
                .stream()
                .sorted(Comparator.comparing(value -> value.getAttribute().getId()))
                .map(value -> new SellerProductItemResponse.VariantAttribute(
                        value.getAttribute().getId(), value.getAttribute().getName(), value.getValue()
                ))
                .toList();

        return new SellerProductItemResponse.Variant(
                variant.getId(), variant.getSku(), variant.getPrice(), variant.getStockQuantity(), attributes
        );
    }

}
