package com.example.auth_service.application.port.out.storage;

import com.example.auth_service.application.constant.Constant;

// Danh sách bucket storage mà auth service được phép sử dụng.
public enum StorageBucket {
    SHOP_LOGO(Constant.AUTH_BUCKET),
    // Dùng chung bucket với logo shop, phân biệt bằng tiền tố object path.
    USER_AVATAR(Constant.AUTH_BUCKET);

    private final String bucketName;

    StorageBucket(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
