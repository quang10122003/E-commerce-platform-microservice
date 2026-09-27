package com.example.producr_service.adapter.mapper;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.adapter.entity.*;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.domain.model.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

@Component
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProductMapper {

    public ProductEntity toEntity(Product product, CategoryEntity category, BrandEntity brand) {
        ProductEntity productEntity = ProductEntity.builder()
                .id(product.getId())
                .userId(product.getUserId())
                .category(category)
                .brand(brand)
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .totalSold(product.getTotalSold())
                .status(product.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attributes(new ArrayList<>())
                .variants(new ArrayList<>())
                .build();

        // map tuong ung: AttributeValue (domain, chua id) -> AttributeValueEntity (vua tao)
        Map<AttributeValue, AttributeValueEntity> AttributeValueEntityMap = new IdentityHashMap<>();

        for (ProductAttribute attribute : product.getAttributes()) {
            AttributeEntity attributeEntity = AttributeEntity.builder()
                    .id(null)
                    .product(productEntity)
                    .name(attribute.getName())
                    .values(new ArrayList<>())
                    .build();
            productEntity.addAttribute(attributeEntity);

            for (AttributeValue attributeValue : attribute.getValues()) {
                AttributeValueEntity attributeValueEntity =
                        new AttributeValueEntity(null, attributeEntity, attributeValue.getValue());
                attributeEntity.addValue(attributeValueEntity);
                AttributeValueEntityMap.put(attributeValue, attributeValueEntity);
            }
        }

        for (ProductVariant productVariant : product.getVariants()) {
            ProductVariantEntity productVariantEntity = ProductVariantEntity.builder()
                    .id(null)
                    .product(productEntity)
                    .sku(productVariant.getSku())
                    .price(productVariant.getPrice().getAmount())
                    .stockQuantity(productVariant.getStockQuantity())
                    .createdAt(LocalDateTime.now())
                    .images(new ArrayList<>())
                    .attributeValues(new HashSet<>())
                    .build();

            productEntity.addVariant(productVariantEntity);

            // link bang OBJECT tra tu map o tren - day la buoc giai quyet dung van de "chua co id"
            for (AttributeValue value : productVariant.getAttributeValues()) {
                AttributeValueEntity attributeValueEntity = AttributeValueEntityMap.get(value);
                productVariantEntity.linkAttributeValue(attributeValueEntity);
            }

            for (VariantImage image : productVariant.getImages()) {
                productVariantEntity.addImage(new VariantImageEntity(
                        null, productVariantEntity, image.getImageUrl(), image.isPrimary()));
            }
        }

        return productEntity;
    }

    // Chuyển entity thành Product và lấy tên danh mục, thương hiệu từ quan hệ đã lưu.
    public Product toDomain(ProductEntity productEntity) {
        return toDomain(productEntity, true);
    }

    // Tải thuộc tính của sản phẩm nhưng để phân loại được truy vấn theo lô riêng.
    private Product toSellerDomain(ProductEntity productEntity) {
        return toDomain(productEntity, false);
    }

    // Chuyển entity sang domain, tùy luồng có cần tải phân loại trực tiếp hay không.
    private Product toDomain(ProductEntity productEntity, boolean includeVariants) {
        Product product = new Product(
                productEntity.getId(), productEntity.getUserId(),
                productEntity.getCategory().getId(), productEntity.getCategory().getName(),
                productEntity.getBrand() != null ? productEntity.getBrand().getId() : null,
                productEntity.getBrand() != null ? productEntity.getBrand().getName() : null,
                productEntity.getName(), productEntity.getDescription(), productEntity.getImageUrl(),
                productEntity.getCreatedAt(), productEntity.getTotalSold()
        );
        // Đồng bộ trạng thái đã lưu từ persistence về domain aggregate.
        if (productEntity.getStatus() == ProductStatus.INACTIVE) {
            product.deactivate();
        }

        Map<AttributeValueEntity, AttributeValue> valueMap = new IdentityHashMap<>();

        for (AttributeEntity attrEntity : productEntity.getAttributes()) {
            ProductAttribute attribute = new ProductAttribute(attrEntity.getId(), attrEntity.getName());
            for (AttributeValueEntity valueEntity : attrEntity.getValues()) {
                AttributeValue value = new AttributeValue(valueEntity.getId(), valueEntity.getValue());
                attribute.addValue(value);
                valueMap.put(valueEntity, value);
            }
            product.addAttribute(attribute);
        }

        if (!includeVariants) {
            return product;
        }

        for (ProductVariantEntity variantEntity : productEntity.getVariants()) {
            ProductVariant variant = new ProductVariant(
                    variantEntity.getId(), variantEntity.getSku(),
                    Money.of(variantEntity.getPrice()), variantEntity.getStockQuantity()
            );
            for (AttributeValueEntity valueEntity : variantEntity.getAttributeValues()) {
                variant.linkAttributeValue(valueMap.get(valueEntity));
            }
            for (VariantImageEntity imgEntity : variantEntity.getImages()) {
                variant.addImage(new VariantImage(
                        imgEntity.getId(), imgEntity.getImageUrl(), imgEntity.isPrimary()));
            }
            product.addVariant(variant);
        }

        return product;
    }

    // Chuyển trang entity sang trang domain và giữ nguyên metadata phân trang.
    public PageResponse<Product> toDomainPage(Page<ProductEntity> productPage) {
        return new PageResponse<>(
                productPage.getContent().stream().map(this::toSellerDomain).toList(),
                productPage.getNumber() + 1,
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    // Chuyển Elasticsearch document thành ProductSearchResponse cho API.
    public ProductSearchResponse toResponse(ProductSearchDocument document) {
        return new ProductSearchResponse(
                document.getId(),
                document.getName(),
                document.getLocation(),
                document.getUserId(),
                document.getDescription(),
                document.getCategoryId(),
                document.getCategoryName(),
                document.getBrandId(),
                document.getBrandName(),
                document.getStatus(),
                document.getImageUrl(),
                document.getMaxPrice(),
                document.getTotalSold()
        );
    }
}
