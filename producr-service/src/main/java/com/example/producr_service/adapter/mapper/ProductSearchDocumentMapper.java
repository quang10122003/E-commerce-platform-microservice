package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.DTO.documentElasticsearch.ProductSearchDocument;
import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductSearchDocumentMapper {
    public ProductSearchDocument toDocument(
            Product product,
            String location
    ) {
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

    private List<ProductSearchDocument.AttributeDocument> toAttributeDocuments(Product product) {
        return product.getAttributes().stream()
                .flatMap(attribute -> toAttributeDocuments(attribute).stream())
                .toList();
    }

    private List<ProductSearchDocument.AttributeDocument> toAttributeDocuments(ProductAttribute attribute) {
        return attribute.getValues().stream()
                .map(value -> ProductSearchDocument.AttributeDocument.builder()
                        .name(attribute.getName())
                        .value(value.getValue())
                        .build())
                .toList();
    }

    private List<ProductSearchDocument.VariantDocument> toVariantDocuments(Product product) {
        return product.getVariants().stream()
                .map(this::toVariantDocument)
                .toList();
    }

    private ProductSearchDocument.VariantDocument toVariantDocument(ProductVariant variant) {
        return ProductSearchDocument.VariantDocument.builder()
                .sku(variant.getSku())
                .price(variant.getPrice().getAmount())
                .stockQuantity(variant.getStockQuantity())
                .build();
    }

}
