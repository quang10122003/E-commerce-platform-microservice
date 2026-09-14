package com.example.producr_service.domain.model;

import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate Root cua Product Service.
 * Product "so huu" ProductAttribute va ProductVariant (quan he CASCADE
 * trong DB), nen chung duoc mo hinh nhu 1 phan cua aggregate nay -
 * moi thay doi doi voi attribute/variant deu di qua Product,
 * khong duoc sua truc tiep tu ben ngoai.
 */
@Getter
public class Product {

    private final Long id;
    private Long categoryId;
    private Long brandId; // co the null - san pham chua ro hang
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;

    private final List<ProductAttribute> attributes = new ArrayList<>();
    private final List<ProductVariant> variants = new ArrayList<>();

    public Product(Long id, Long categoryId, Long brandId, String name,
                   String description, String imageUrl) {
        if (categoryId == null) {
            throw new BusinessException(DomainProductError.PRODUCT_CATEGORY_REQUIRED);
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainProductError.PRODUCT_NAME_REQUIRED);
        }
        this.id = id;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.active = true; // mac dinh mo ban khi vua tao, giong DEFAULT TRUE trong DB
    }

    public List<ProductAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    // ----- hanh vi nghiep vu -----

    public void addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
    }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
    }

    /** Sản phẩm không phân loại thì danh sách attributes rỗng - trạng thái bình thường. */
    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateBasicInfo(String name, String description, String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainProductError.PRODUCT_NAME_REQUIRED);
        }
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public void changeBrand(Long brandId) {
        this.brandId = brandId;
    }

    public void moveToCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(DomainProductError.PRODUCT_CATEGORY_REQUIRED);
        }
        this.categoryId = categoryId;
    }

    // tìm product_variant phuf hợp với attributeValues
    public Optional<ProductVariant> findVariantByAttributeValues(Set<AttributeValue> attributeValues) {
        return variants.stream()
                .filter(v -> v.matchesAllAttributeValues(attributeValues))
                .findFirst();
    }

    /** Khoang gia hien thi tren the card - "150.000d" hoac "Tu 150.000d". */
    public PriceRange getPriceRange() {
        if (variants.isEmpty()) {
            throw new BusinessException(DomainProductError.PRODUCT_VARIANTS_REQUIRED);
        }
        Money min = variants.getFirst().getPrice();
        Money max = variants.getFirst().getPrice();
        for (ProductVariant v : variants) {
            if (v.getPrice().isLessThan(min)) min = v.getPrice();
            if (v.getPrice().isGreaterThan(max)) max = v.getPrice();
        }
        return new PriceRange(min, max);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** Value object nho gon cho khoang gia thap nhat - cao nhat cua 1 san pham. */
    @Getter
    public static class PriceRange {
        private final Money min;
        private final Money max;

        public PriceRange(Money min, Money max) {
            this.min = min;
            this.max = max;
        }

        public boolean isSinglePrice() {
            return min.equals(max);
        }
    }
}
