package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class ProductVariant {

    public static final long MINIMUM_PRICE = 1_000L;

    @Getter
    private final Long id;
    @Getter
    private final String sku;
    @Getter
    private Money price;
    @Getter
    private int stockQuantity;
    private final Set<AttributeValue> attributeValues = new HashSet<>();
    private final List<VariantImage> images = new ArrayList<>();

    public ProductVariant(Long id, String sku, Money price, int stockQuantity) {
        this(id, sku, stockQuantity);
        validatePrice(price);
        this.price = price;
    }

    private ProductVariant(Long id, String sku, int stockQuantity) {
        if (sku == null || sku.isBlank()) {
            throw new BusinessException(DomainProductError.SKU_REQUIRED);
        }
        if (stockQuantity < 0) {
            throw new BusinessException(DomainProductError.STOCK_QUANTITY_NEGATIVE);
        }
        this.id = id;
        this.sku = sku;
        this.stockQuantity = stockQuantity;
    }

    // Khôi phục trạng thái đã lưu, không áp lại điều kiện tạo mới hoặc thay đổi giá.
    public static ProductVariant reconstitute(Long id, String sku, Money price, int stockQuantity) {
        ProductVariant variant = new ProductVariant(id, sku, stockQuantity);
        variant.price = Objects.requireNonNull(price, "Giá phân loại không được null");
        return variant;
    }

    public Set<AttributeValue> getAttributeValues() {
        return Collections.unmodifiableSet(attributeValues);
    }

    public List<VariantImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    public void linkAttributeValue(AttributeValue value) {
        attributeValues.add(value);
    }

    public void addImage(VariantImage image) {
        // Bảo vệ invariant: mỗi variant chỉ có tối đa một ảnh chính.
        if (image.isPrimary() && images.stream().anyMatch(VariantImage::isPrimary)) {
            throw new BusinessException(DomainProductError.VARIANT_MULTIPLE_PRIMARY_IMAGES);
        }
        images.add(image);
    }



    // check product_varinat có đủ atrirbute value yêu cầu k
    public boolean matchesAllAttributeValues(Set<AttributeValue> requiredValues) {
        return this.attributeValues.containsAll(requiredValues);
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(DomainProductError.STOCK_DECREASE_QUANTITY_INVALID);
        }
        if (quantity > stockQuantity) {
            throw new BusinessException(DomainProductError.STOCK_INSUFFICIENT);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(DomainProductError.STOCK_INCREASE_QUANTITY_INVALID);
        }
        this.stockQuantity += quantity;
    }

    public void changePrice(Money newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    //  check giá sản phẩm phải > 1000
    private static void validatePrice(Money price) {
        if (price == null || price.getAmount().compareTo(BigDecimal.valueOf(MINIMUM_PRICE)) < 0) {
            throw new BusinessException(DomainProductError.PRODUCT_VARIANT_PRICE_INVALID);
        }
    }

    public VariantImage getPrimaryImage() {
        return images.stream()
                .filter(VariantImage::isPrimary)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductVariant that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
