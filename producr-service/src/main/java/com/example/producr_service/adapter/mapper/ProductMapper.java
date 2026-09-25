package com.example.producr_service.adapter.mapper;
import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.adapter.entity.*;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.ProductSearchResponse;
import com.example.producr_service.application.dto.response.SellerProductItemResponse;
import com.example.producr_service.domain.model.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Component
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProductMapper {
    ProductVariantMapper productVariantMapper;

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

    /** ProductEntity (da co du id sau khi luu/doc tu DB) -> Product (domain). */
    public Product toDomain(ProductEntity productEntity) {
        Product product = new Product(
                productEntity.getId(), productEntity.getUserId(),productEntity.getCategory().getId(),
                productEntity.getBrand() != null ? productEntity.getBrand().getId() : null,
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
                productPage.getContent().stream().map(this::toDomain).toList(),
                productPage.getNumber() + 1,
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages()
        );
    }

    // map product entity  và ProductVariantEntity thành repone DTO
    public SellerProductItemResponse toSellerItem(
            ProductEntity product,
            List<ProductVariantEntity> variants
    ) {
        // Sắp xếp phân loại theo ID để API luôn trả về cùng thứ tự.
        List<SellerProductItemResponse.Variant> variantResponses = variants.stream()
                .sorted(Comparator.comparing(ProductVariantEntity::getId))
                .map(productVariantMapper::toSellerVariant)
                .toList();

        // Hết hàng được ưu tiên cho cả sản phẩm đang bán và đang ẩn.
        boolean inStock = variants.stream().anyMatch(variant -> variant.getStockQuantity() > 0);
        SellerProductItemResponse.DisplayStatus displayStatus = !inStock
                ? SellerProductItemResponse.DisplayStatus.OUT_OF_STOCK
                : product.getStatus() == ProductStatus.INACTIVE
                        ? SellerProductItemResponse.DisplayStatus.INACTIVE
                        : SellerProductItemResponse.DisplayStatus.ACTIVE;

        return new SellerProductItemResponse(
                product.getId(), product.getName(), product.getImageUrl(), product.getCreatedAt(),
                product.getCategory().getId(), product.getCategory().getName(), displayStatus, variantResponses
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
