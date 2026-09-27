package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.List;

// mapper map doamin với document của documentElasticsearch
@Component
public class ProductSearchDocumentMapper {
    // Chuyển Product cùng tên danh mục và thương hiệu trong domain thành document Elasticsearch.
    public ProductSearchDocument toDocument(
            Product product,
            String location
    ) {
        // Lấy khoảng giá đã được domain Product tính từ các variant.
        Product.PriceRange priceRange = product.getPriceRange();

        return ProductSearchDocument.builder()
                .id(product.getId())
                .productId(product.getId())
                .userId(product.getUserId())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .categoryName(product.getCategoryName())
                .brandId(product.getBrandId())
                .brandName(product.getBrandName())
                .status(product.getStatus())
                .imageUrl(product.getImageUrl())
                .totalSold(product.getTotalSold())
                .minPrice(priceRange.getMin().getAmount())
                .maxPrice(priceRange.getMax().getAmount())
                .createdAt(product.getCreatedAt())
                .attributes(toAttributeDocuments(product))
                .variants(toVariantDocuments(product))
                .location(location)
                .build();
    }

    // Chuyển các attribute và value của Product thành dữ liệu nested để filter.
    private List<ProductSearchDocument.AttributeDocument> toAttributeDocuments(Product product) {
        return product.getAttributes().stream()
                .flatMap(attribute -> toAttributeDocuments(attribute).stream())
                .toList();
    }

    // Chuyển các value của một attribute thành các document nested.
    private List<ProductSearchDocument.AttributeDocument> toAttributeDocuments(ProductAttribute attribute) {
        return attribute.getValues().stream()
                .map(value -> ProductSearchDocument.AttributeDocument.builder()
                        .name(attribute.getName())
                        .value(value.getValue())
                        .build())
                .toList();
    }

    // Chuyển toàn bộ variant sang dữ liệu phục vụ filter giá và SKU.
    private List<ProductSearchDocument.VariantDocument> toVariantDocuments(Product product) {
        return product.getVariants().stream()
                .map(this::toVariantDocument)
                .toList();
    }

    // Chuyển một variant thành document nested.
    private ProductSearchDocument.VariantDocument toVariantDocument(ProductVariant variant) {
        return ProductSearchDocument.VariantDocument.builder()
                .sku(variant.getSku())
                .price(variant.getPrice().getAmount())
                .stockQuantity(variant.getStockQuantity())
                .build();
    }

}
