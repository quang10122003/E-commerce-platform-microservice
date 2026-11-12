package com.example.order_service.application.port.out.storage;

import com.example.order_service.application.constant.Constant;

public enum StorageBucket {
    REVIEWS(Constant.REVIEW_BUCKET);

    private final String bucketName;

    StorageBucket(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getBucketName() {
        return bucketName;
    }
}
