package com.example.producr_service.adapter.mapper;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.adapter.entity.*;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.dto.response.ShopProductDetailResponse;
import com.example.producr_service.application.port.out.ShopProductDetailMapperPort;
import com.example.producr_service.application.port.out.storage.StorageBucket;
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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProductMapper implements ShopProductDetailMapperPort {

    // Trả ID, SKU và ảnh cần cho form chỉnh sửa nhưng không lộ đường dẫn storage.
    @Override
    public ShopProductDetailResponse toShopProductDetailResponse(Product product) {
        List<ProductAttribute> sourceAttributes = product.getAttributes();
        List<ShopProductDetailResponse.Attribute> attributes = sourceAttributes.stream()
                .map(attribute -> new ShopProductDetailResponse.Attribute(
                        attribute.getId(), attribute.getName(),
                        attribute.getValues().stream()
                                .map(value -> new ShopProductDetailResponse.Value(
                                        value.getId(), value.getValue()))
                                .toList()))
                .toList();
        List<ShopProductDetailResponse.Variant> variants = product.getVariants().stream()
                .map(variant -> toShopVariantResponse(variant, sourceAttributes))
                .toList();
        return new ShopProductDetailResponse(product.getId(), product.getCategoryId(),
                product.getBrandId(), product.getName(), product.getDescription(),
                product.getImageUrl(), attributes, variants);
    }

    // Tìm vị trí từng giá trị đã chọn để frontend gửi lại đúng tổ hợp phân loại.
    private ShopProductDetailResponse.Variant toShopVariantResponse(
            ProductVariant variant, List<ProductAttribute> attributes) {
        List<ShopProductDetailResponse.Selection> selections = new ArrayList<>();
        for (int attributeIndex = 0; attributeIndex < attributes.size(); attributeIndex++) {
            List<AttributeValue> values = attributes.get(attributeIndex).getValues();
            for (int valueIndex = 0; valueIndex < values.size(); valueIndex++) {
                if (variant.getAttributeValues().contains(values.get(valueIndex))) {
                    selections.add(new ShopProductDetailResponse.Selection(attributeIndex, valueIndex));
                }
            }
        }
        return new ShopProductDetailResponse.Variant(
                variant.getId(), variant.getSku(), variant.getPrice().getAmount(),
                variant.getStockQuantity(), selections,
                variant.getImages().stream()
                        .map(image -> new ShopProductDetailResponse.Image(
                                image.getId(), image.getImageUrl(), image.isPrimary()))
                        .toList());
    }

    // Cập nhật entity đang quản lý để giữ ID phân loại, thuộc tính và ảnh còn dùng.
    public void updateEntity(Product product, ProductEntity entity,
                             CategoryEntity category, BrandEntity brand) {
        entity.setCategory(category);
        entity.setBrand(brand);
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setImageUrl(product.getImageUrl());
        entity.setObjectPath(product.getObjectPath());
        entity.setUpdatedAt(LocalDateTime.now());

        Map<Long, AttributeEntity> existingAttributes = new HashMap<>();
        for (AttributeEntity attribute : entity.getAttributes()) existingAttributes.put(attribute.getId(), attribute);
        Set<Long> retainedAttributeIds = new HashSet<>();
        Map<AttributeEntity, Set<Long>> retainedValueIdsByAttribute = new IdentityHashMap<>();
        Map<AttributeValue, AttributeValueEntity> values = new IdentityHashMap<>();
        for (ProductAttribute source : product.getAttributes()) {
            AttributeEntity attribute = source.getId() == null ? null : existingAttributes.get(source.getId());
            if (attribute == null) {
                attribute = AttributeEntity.builder().product(entity).name(source.getName())
                        .values(new ArrayList<>()).build();
                entity.addAttribute(attribute);
            } else {
                retainedAttributeIds.add(attribute.getId());
                attribute.setName(source.getName());
            }
            Map<Long, AttributeValueEntity> existingValues = new HashMap<>();
            for (AttributeValueEntity value : attribute.getValues()) existingValues.put(value.getId(), value);
            Set<Long> retainedValueIds = new HashSet<>();
            for (AttributeValue sourceValue : source.getValues()) {
                AttributeValueEntity value = sourceValue.getId() == null
                        ? null : existingValues.get(sourceValue.getId());
                if (value == null) {
                    value = new AttributeValueEntity(null, attribute, sourceValue.getValue());
                    attribute.addValue(value);
                } else {
                    retainedValueIds.add(value.getId());
                    value.setValue(sourceValue.getValue());
                }
                values.put(sourceValue, value);
            }
            retainedValueIdsByAttribute.put(attribute, retainedValueIds);
        }

        Map<Long, ProductVariantEntity> existingVariants = new HashMap<>();
        for (ProductVariantEntity variant : entity.getVariants()) existingVariants.put(variant.getId(), variant);
        for (ProductVariant source : product.getVariants()) {
            ProductVariantEntity variant = source.getId() == null ? null : existingVariants.get(source.getId());
            if (variant == null) {
                variant = ProductVariantEntity.builder().product(entity).sku(source.getSku())
                        .createdAt(LocalDateTime.now()).images(new ArrayList<>())
                        .attributeValues(new HashSet<>()).build();
                entity.addVariant(variant);
            }
            variant.setSku(source.getSku());
            variant.setPrice(source.getPrice().getAmount());
            variant.setStockQuantity(source.getStockQuantity());
            variant.getAttributeValues().clear();
            for (AttributeValue sourceValue : source.getAttributeValues()) {
                variant.linkAttributeValue(values.get(sourceValue));
            }
            Map<Long, VariantImageEntity> existingImages = new HashMap<>();
            for (VariantImageEntity image : variant.getImages()) existingImages.put(image.getId(), image);
            Set<Long> retainedImageIds = new HashSet<>();
            for (VariantImage sourceImage : source.getImages()) {
                VariantImageEntity image = sourceImage.getId() == null
                        ? null : existingImages.get(sourceImage.getId());
                if (image == null) {
                    image = new VariantImageEntity(null, variant, sourceImage.getImageUrl(),
                            sourceImage.getObjectPath(), StorageBucket.PRODUCT_VARIANTS,
                            sourceImage.isPrimary());
                    variant.addImage(image);
                } else {
                    retainedImageIds.add(image.getId());
                    image.setPrimary(sourceImage.isPrimary());
                }
            }
            variant.getImages().removeIf(image -> image.getId() != null
                    && !retainedImageIds.contains(image.getId()));
        }
        // Gỡ value cũ sau khi mọi phân loại đã chuyển sang tổ hợp mới.
        retainedValueIdsByAttribute.forEach((attribute, retainedValueIds) ->
                attribute.getValues().removeIf(value -> value.getId() != null
                        && !retainedValueIds.contains(value.getId())));
        entity.getAttributes().removeIf(attribute -> attribute.getId() != null
                && !retainedAttributeIds.contains(attribute.getId()));
    }

    public ProductEntity toEntity(Product product, CategoryEntity category, BrandEntity brand) {
        ProductEntity productEntity = ProductEntity.builder()
                .id(product.getId())
                .userId(product.getUserId())
                .category(category)
                .brand(brand)
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .objectPath(product.getObjectPath())
                .storageBucket(StorageBucket.PRODUCT_IMAGES)
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
                        null, productVariantEntity, image.getImageUrl(),
                        image.getObjectPath(),
                        StorageBucket.PRODUCT_VARIANTS, image.isPrimary()));
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
                productEntity.getCreatedAt(), productEntity.getTotalSold(),
                productEntity.getObjectPath()
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
                        imgEntity.getId(), imgEntity.getImageUrl(),
                        imgEntity.getObjectPath(), imgEntity.isPrimary()));
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
