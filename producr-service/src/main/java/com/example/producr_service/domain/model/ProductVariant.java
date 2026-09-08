package com.example.producr_service.domain.model;

import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.VariantImage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Ung voi bang product_variants.
 * Quan he nhieu-nhieu voi AttributeValue (qua bang noi
 * variant_attribute_values) duoc bieu dien don gian bang 1 Set<Long>
 * chua id cua cac AttributeValue - domain khong can biet ve su ton tai
 * cua "bang noi", do la chi tiet ky thuat cua tang persistence.
 */
@Getter
public class ProductVariant {

    private final Long id;
    private final String sku;
    private Money price;
    private int stockQuantity;
    private final Set<Long> attributeValueIds = new HashSet<>();
    private final List<VariantImage> images = new ArrayList<>();

    public ProductVariant(Long id, String sku, Money price, int stockQuantity) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU khong duoc rong");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Ton kho khong duoc am");
        }
        this.id = id;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }


    public Set<Long> getAttributeValueIds() {
        return Collections.unmodifiableSet(attributeValueIds);
    }

    public List<VariantImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    public void linkAttributeValue(Long attributeValueId) {
        attributeValueIds.add(attributeValueId);
    }

    public void addImage(VariantImage image) {
        images.add(image);
    }

    /**
     * Kiem tra variant nay co khop DU CA TAP gia tri thuoc tinh duoc yeu cau
     * hay khong.
     */
    public boolean matchesAllAttributeValues(Set<Long> requiredValueIds) {
        return this.attributeValueIds.containsAll(requiredValueIds);
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("So luong tru kho phai > 0");
        }
        if (quantity > stockQuantity) {
            throw new IllegalStateException("Khong du ton kho de tru");
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("So luong nhap kho phai > 0");
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
        if (!(o instanceof ProductVariant)) return false;
        ProductVariant that = (ProductVariant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
