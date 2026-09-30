package com.example.producr_service.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;
import com.example.producr_service.application.constant.Constant;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.repo.ProductVariantRepoPort;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.port.out.storage.StoredFile;
import com.example.producr_service.domain.service.SkuGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductHelper {
    ProductRepositoryPort productRepositoryPort;
    ProductVariantRepoPort productVariantRepoPort;

    // Chặn truy vấn catalog khi client không truyền từ khóa tìm kiếm.
    public void validateSearchKeyword(String keyword) {
        if (!ValidationUtils.hasText(keyword)) {
            throw new BusinessException(
                    ProductError.SEARCH_KEYWORD_REQUIRED,
                    "keyword khong duoc de trong"
            );
        }
    }

    // Kiểm tra khoảng giá trước khi gửi query tìm kiếm sang Elasticsearch.
    public void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BusinessException(
                    ProductError.INVALID_PRICE_RANGE,
                    "minPrice khong duoc lon hon maxPrice"
            );
        }
    }

    // Khóa sản phẩm và xác nhận người dùng là chủ sở hữu.
    public void validateProductOwnership(Long productId, Long userId) {
        Long ownerId = productRepositoryPort.findOwnerIdByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ProductError.PRODUCT_NOT_FOUND));
        if (!ownerId.equals(userId)) {
            throw new BusinessException(AuthorizationError.ACCESS_DENIED);
        }
    }

    // Kiểm tra metadata và loại trùng ảnh trước khi ghi tác vụ dọn storage.
    public List<StoredFile> prepareFilesForDeletion(Long productId, List<StoredFile> listStoredFile) {
        List<StoredFile> files = new ArrayList<>();
        Map<StorageBucket, Set<String>> seenPaths = new EnumMap<>(StorageBucket.class);

        for (StoredFile file : listStoredFile) {
            if (file == null || file.bucket() == null
                    || !ValidationUtils.hasText(file.objectPath())
                    || !ValidationUtils.hasText(file.publicUrl())) {
                throw new IllegalStateException("Metadata ảnh không hợp lệ: productId=" + productId);
            }

            Set<String> paths = seenPaths.computeIfAbsent(file.bucket(), bucket -> new HashSet<>());
            if (paths.add(file.objectPath())) {
                files.add(file);
            }
        }

        return List.copyOf(files);
    }
    // lấy tên tỉnh của shop
    public String extractProvinceNameFromAddress(String shopAddress) {
        return shopAddress == null || shopAddress.isBlank()
                ? null
                : shopAddress.substring(shopAddress.lastIndexOf(",") + 1).trim();
    }

    // Sinh SKU duy nhất theo quy tắc domain và loại trừ bản ghi đang được sửa.
    public String generateUniqueSku(String productName, List<String> values,
                                    Long currentVariantId, Set<String> usedSkus) {
        String generated = SkuGenerator.generateBase(productName, values);
        String base = generated.substring(0, Math.min(generated.length(), Constant.MAX_SKU_LENGTH));
        String candidate = base;
        Long excludedId = currentVariantId == null ? -1L : currentVariantId;
        for (int attempt = 0; attempt <= Constant.MAX_SKU_RETRY; attempt++) {
            if (!usedSkus.contains(candidate)
                    && !productVariantRepoPort.existsBySkuAndIdNot(candidate, excludedId)) {
                usedSkus.add(candidate);
                return candidate;
            }
            candidate = base.substring(0, Math.min(base.length(),
                    Constant.MAX_SKU_LENGTH - Constant.SKU_RANDOM_SUFFIX_LENGTH - 1))
                    + "-" + SkuGenerator.randomSuffix(Constant.SKU_RANDOM_SUFFIX_LENGTH);
        }
        throw new BusinessException(ProductError.INVALID_PRODUCT_UPDATE,
                "Khong the sinh SKU duy nhat");
    }

}
