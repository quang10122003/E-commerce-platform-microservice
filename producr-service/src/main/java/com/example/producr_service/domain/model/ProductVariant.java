package com.example.producr_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class ProductVariant {

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
        if (sku == null || sku.isBlank()) {
            throw new BusinessException(DomainProductError.SKU_REQUIRED);
        }
        if (stockQuantity < 0) {
            throw new BusinessException(DomainProductError.STOCK_QUANTITY_NEGATIVE);
        }
        this.id = id;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
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
        this.price = newPrice;
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
