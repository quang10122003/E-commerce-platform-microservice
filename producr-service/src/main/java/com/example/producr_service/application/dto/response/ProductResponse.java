package com.example.producr_service.application.dto.response;

import com.example.producr_service.domain.model.Product;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.producr_service.domain.model.ProductStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProductResponse {

    private Long id;
    private Long categoryId;
    // Tên danh mục tại thời điểm đọc sản phẩm.
    private String categoryName;
    private Long brandId;
    // Tên thương hiệu, để trống khi sản phẩm không gắn thương hiệu.
    private String brandName;
    private String name;
    private String description;
    private String imageUrl;
    private ProductStatus status;
    // Hiển thị tổng số lượng product đã bán.
    private long totalSold;
    private List<VariantResponse> variants;

    // dung factory method thay vi constructor dai - de doc va an toan khi doi thu tu field
    public static ProductResponse from(Product product) {
        ProductResponse response = new ProductResponse();
        response.id = product.getId();
        response.categoryId = product.getCategoryId();
        response.categoryName = product.getCategoryName();
        response.brandId = product.getBrandId();
        response.brandName = product.getBrandName();
        response.name = product.getName();
        response.description = product.getDescription();
        response.imageUrl = product.getImageUrl();
        response.status = product.getStatus();
        response.totalSold = product.getTotalSold();
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
