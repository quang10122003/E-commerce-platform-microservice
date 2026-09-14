package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductVariant;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProductResponse {

    private Long id;
    private Long categoryId;
    private Long brandId;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;
    private List<VariantResponse> variants;

    // dung factory method thay vi constructor dai - de doc va an toan khi doi thu tu field
    public static ProductResponse from(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.getId();
        response.categoryId = product.getCategoryId();
        response.brandId = product.getBrandId();
        response.name = product.getName();
        response.description = product.getDescription();
        response.imageUrl = product.getImageUrl();
        response.active = product.isActive();
        response.variants = product.getVariants().stream()
                .map(VariantResponse::from)
                .collect(Collectors.toList());
        return response;
    }

    @Getter
    public static class VariantResponse {
        private Long id;
        private String sku;
        private BigDecimal price;
        private int stockQuantity;

        public static VariantResponse from(ProductVariant variant) {
            VariantResponse r = new VariantResponse();
            r.id = variant.getId();
            r.sku = variant.getSku();
            r.price = variant.getPrice().getAmount();
            r.stockQuantity = variant.getStockQuantity();
            return r;
        }

    }
}