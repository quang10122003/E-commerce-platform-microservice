package com.example.producr_service.domain.model;

import com.example.producr_service.domain.model.Money;
import com.example.producr_service.domain.model.ProductAttribute;
import com.example.producr_service.domain.model.ProductVariant;
import com.example.common.exception.BusinessException;
import com.example.producr_service.domain.error.DomainProductError;
import lombok.Getter;

import java.time.LocalDateTime;
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
    private final Long userId;
    private Long categoryId;
    // Lưu tên danh mục tương ứng với categoryId để phục vụ các luồng đọc sản phẩm.
    private String categoryName;
    private Long brandId; // co the null - san pham chua ro hang
    // Lưu tên thương hiệu tương ứng với brandId, có thể null.
    private String brandName;
    private String name;
    private String description;
    private String imageUrl;
    // Lưu đường dẫn ảnh bìa trên storage.
    private String objectPath;
    private ProductStatus status;
    // Lưu tổng số lượng sản phẩm đã bán của toàn bộ variant.
    private long totalSold;
    private final LocalDateTime createdAt;

    private final List<ProductAttribute> attributes = new ArrayList<>();
    private final List<ProductVariant> variants = new ArrayList<>();

    // Tạo sản phẩm mới với mã và tên danh mục, thương hiệu.
    public Product(Long id, Long userId, Long categoryId, String categoryName,
                   Long brandId, String brandName, String name,
                   String description, String imageUrl) {
        this(id, userId, categoryId, categoryName, brandId, brandName, name,
                description, imageUrl, null);
    }

    // Khởi tạo aggregate từ dữ liệu persistence, bao gồm thời điểm tạo product.
    public Product(Long id, Long userId, Long categoryId, String categoryName,
                   Long brandId, String brandName, String name,
                   String description, String imageUrl, LocalDateTime createdAt) {
        this(id, userId, categoryId, categoryName, brandId, brandName,
                name, description, imageUrl, createdAt, 0L);
    }

    // Khởi tạo aggregate từ dữ liệu persistence, bao gồm tổng số lượng đã bán.
    public Product(Long id, Long userId, Long categoryId, String categoryName,
                   Long brandId, String brandName, String name,
                   String description, String imageUrl, LocalDateTime createdAt, long totalSold) {
        this(id, userId, categoryId, categoryName, brandId, brandName, name,
                description, imageUrl, createdAt, totalSold, null);
    }

    // Giữ đường dẫn ảnh đã upload cùng aggregate để lưu trữ và dọn ảnh khi cần.
    public Product(Long id, Long userId, Long categoryId, String categoryName,
                   Long brandId, String brandName, String name,
                   String description, String imageUrl, LocalDateTime createdAt,
                   long totalSold, String objectPath) {
        if (userId == null) {
            throw new BusinessException(DomainProductError.PRODUCT_USER_REQUIRED);
        }

        if (categoryId == null) {
            throw new BusinessException(DomainProductError.PRODUCT_CATEGORY_REQUIRED);
        }
        if (categoryName == null || categoryName.isBlank()) {
            throw new BusinessException(DomainProductError.CATEGORY_NAME_REQUIRED);
        }
        if (brandId != null && (brandName == null || brandName.isBlank())) {
            throw new BusinessException(DomainProductError.BRAND_NAME_REQUIRED);
        }
        if (brandId == null && brandName != null) {
            throw new IllegalArgumentException("Tên thương hiệu cần có mã thương hiệu");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainProductError.PRODUCT_NAME_REQUIRED);
        }
        // Ảnh đại diện là dữ liệu bắt buộc của sản phẩm.
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(DomainProductError.IMAGE_URL_REQUIRED);
        }

        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.brandId = brandId;
        this.brandName = brandName;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.objectPath = objectPath;
        this.createdAt = createdAt;
        this.totalSold = totalSold;
        // Product mới được tạo mặc định ở trạng thái hoạt động.
        this.status = ProductStatus.ACTIVE;
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
        this.status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void updateBasicInfo(String name, String description, String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(DomainProductError.PRODUCT_NAME_REQUIRED);
        }
        // Không cho phép cập nhật sản phẩm về trạng thái thiếu ảnh đại diện.
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(DomainProductError.IMAGE_URL_REQUIRED);
        }
        this.name = name;
        this.description = description;
        // Tránh giữ object path của ảnh cũ khi URL ảnh thay đổi.
        if (!Objects.equals(this.imageUrl, imageUrl)) {
            this.objectPath = null;
        }
        this.imageUrl = imageUrl;
    }

    // Giữ mã và tên thương hiệu đồng bộ khi thay đổi thương hiệu.
    public void changeBrand(Long brandId, String brandName) {
        if (brandId != null && (brandName == null || brandName.isBlank())) {
            throw new BusinessException(DomainProductError.BRAND_NAME_REQUIRED);
        }
        if (brandId == null && brandName != null) {
            throw new IllegalArgumentException("Tên thương hiệu cần có mã thương hiệu");
        }
        this.brandId = brandId;
        this.brandName = brandName;
    }

    // Giữ mã và tên danh mục đồng bộ khi chuyển sản phẩm sang danh mục khác.
    public void moveToCategory(Long categoryId, String categoryName) {
        if (categoryId == null) {
            throw new BusinessException(DomainProductError.PRODUCT_CATEGORY_REQUIRED);
        }
        if (categoryName == null || categoryName.isBlank()) {
            throw new BusinessException(DomainProductError.CATEGORY_NAME_REQUIRED);
        }
        this.categoryId = categoryId;
        this.categoryName = categoryName;
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
