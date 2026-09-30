package com.example.producr_service.application.service;

import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;
import com.example.producr_service.application.error.ProductError;
import com.example.producr_service.application.port.out.repo.ProductRepositoryPort;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.application.port.out.storage.StoredFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProductHelper {
    private final ProductRepositoryPort productRepositoryPort;

    public ProductHelper(ProductRepositoryPort productRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
    }

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

}
