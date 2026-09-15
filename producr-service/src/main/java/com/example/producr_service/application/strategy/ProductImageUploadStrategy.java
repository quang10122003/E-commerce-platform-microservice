package com.example.producr_service.application.strategy;

import com.example.producr_service.application.port.out.storage.StorageBucket;

// Chuẩn bị ảnh bìa product với bucket và đường dẫn riêng.
public class ProductImageUploadStrategy extends AbstractImageUploadStrategy {

    public ProductImageUploadStrategy() {
        super(UploadPurpose.PRODUCT_IMAGE, StorageBucket.PRODUCT_IMAGES, "Product");
    }
}
