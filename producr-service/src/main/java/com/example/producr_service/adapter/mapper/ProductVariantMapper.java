package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.AttributeValueEntity;
import com.example.producr_service.adapter.entity.ProductVariantEntity;
import com.example.producr_service.adapter.entity.VariantImageEntity;
import com.example.producr_service.application.dto.response.CartInfoRepone;
import com.example.producr_service.application.dto.response.VariantCartView;
import com.example.producr_service.application.dto.response.VariantPriceView;
import com.example.producr_service.adapter.out.persistence.projection.VariantPriceProjection;
import com.example.producr_service.domain.model.AttributeValue;
import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.producr_service.domain.model.VariantImage;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProductVariantMapper {

    public VariantPriceView toPriceView(VariantPriceProjection projection) {
        return new VariantPriceView(projection.getVariantId(), projection.getPrice(),
                projection.getStockQuantity(), projection.getProductStatus());
    }

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

        ProductVariant variant = ProductVariant.reconstitute(
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
        return new VariantImage(entity.getId(), entity.getImageUrl(),
                entity.getObjectPath(), entity.isPrimary());
    }

    // Chỉ đổi hình dạng dữ liệu cho giỏ hàng; không sắp xếp, không chọn ảnh.
    public VariantCartView toCartView(ProductVariantEntity variant) {
        return new VariantCartView(
                variant.getId(),
                variant.getProduct().getId(),
                variant.getProduct().getUserId(),
                variant.getProduct().getName(),
                variant.getProduct().getImageUrl(),
                variant.getProduct().getStatus(),
                variant.getPrice(),
                variant.getStockQuantity(),
                variant.getAttributeValues().stream()
                        .map(value -> new VariantCartView.OptionView(
                                value.getAttribute().getId(), value.getId(),
                                value.getAttribute().getName(), value.getValue()))
                        .toList(),
                variant.getImages().stream()
                        .map(image -> new VariantCartView.ImageView(
                                image.getId(), image.getImageUrl(), image.isPrimary()))
                        .toList());
    }
}
