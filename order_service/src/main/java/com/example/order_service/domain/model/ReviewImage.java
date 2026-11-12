package com.example.order_service.domain.model;

import com.example.common.exception.BusinessException;
import com.example.order_service.domain.error.ReviewDomainError;

// Ảnh đính kèm đánh giá đã lưu trên Storage; object_path và bucket dùng để xóa file khi cần.
public final class ReviewImage {
    private final Long id;
    private final String imageUrl;
    private final String objectPath;
    private final String storageBucket;
    private final int sortOrder;

    // Kiểm tra URL, đường dẫn, bucket không rỗng, đúng độ dài cột và thứ tự ảnh nằm trong 0–4.
    public ReviewImage(Long id, String imageUrl, String objectPath, String storageBucket, int sortOrder) {
        if (isBlank(imageUrl, 500) || isBlank(objectPath, 500) || isBlank(storageBucket, 50)
                || sortOrder < 0 || sortOrder >= ProductReview.MAX_IMAGES) {
            throw new BusinessException(ReviewDomainError.INVALID_DATA);
        }
        this.id = id;
        this.imageUrl = imageUrl;
        this.objectPath = objectPath;
        this.storageBucket = storageBucket;
        this.sortOrder = sortOrder;
    }

    private static boolean isBlank(String value, int maxLength) {
        return value == null || value.isBlank() || value.length() > maxLength;
    }

    public Long getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getObjectPath() {
        return objectPath;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
