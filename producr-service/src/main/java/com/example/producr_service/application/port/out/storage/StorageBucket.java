package com.example.producr_service.application.port.out.storage;

import com.example.producr_service.application.constant.Constant;

import lombok.Getter;

// Danh sách bucket storage mà product service được phép sử dụng.
@Getter
public enum StorageBucket {
    CATEGORY(Constant.CATEGORY_BUCKET),
    PRODUCT_IMAGES(Constant.PRODUCT_IMAGES_BUCKET),
    PRODUCT_VARIANTS(Constant.PRODUCT_VARIANTS_BUCKET);

    private final String bucketName;

    StorageBucket(String bucketName) {
        this.bucketName = bucketName;
    }

}
