package com.example.producr_service.application.strategy;

import com.example.producr_service.application.port.out.storage.StorageBucket;

// Chuẩn bị ảnh category với bucket và đường dẫn riêng.
public class CategoryImageUploadStrategy extends AbstractImageUploadStrategy {

    public CategoryImageUploadStrategy() {
        super(UploadPurpose.CATEGORY_IMAGE, StorageBucket.CATEGORY, "Category");
    }
}
