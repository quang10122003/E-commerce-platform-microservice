package com.example.producr_service.adapter.mapper;
import com.example.producr_service.adapter.entity.*;
import com.example.producr_service.domain.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

@Component
public class ProductMapper {

    /**
     * Domain Product -> ProductEntity (dung khi TAO MOI hoac CAP NHAT).
     *
     * DIEM MAU CHOT: AttributeValue (domain) chua co id luc nay, nen
     * KHONG the tra cuu AttributeValueEntity bang id. Thay vao do,
     * dung 1 IdentityHashMap de "nho" xem 1 AttributeValue (domain)
     * tuong ung voi AttributeValueEntity nao vua duoc tao trong CHINH
     * lan chuyen doi nay - roi dung map do de link ProductVariantEntity
     * dung entity object, JPA se tu INSERT vao bang variant_attribute_values
     * khi save (Hibernate tu sap xep thu tu INSERT hop ly).
     */
    public ProductEntity toEntity(Product product, CategoryEntity category, BrandEntity brand) {
        ProductEntity productEntity = ProductEntity.builder()
                .id(product.getId())
                .category(category)
                .brand(brand)
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
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

    /** ProductEntity (da co du id sau khi luu/doc tu DB) -> Product (domain). */
    public Product toDomain(ProductEntity productEntity) {
        Product product = new Product(
                productEntity.getId(), productEntity.getCategory().getId(),
                productEntity.getBrand() != null ? productEntity.getBrand().getId() : null,
                productEntity.getName(), productEntity.getDescription(), productEntity.getImageUrl()
        );
        if (!productEntity.isActive()) {
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
}
