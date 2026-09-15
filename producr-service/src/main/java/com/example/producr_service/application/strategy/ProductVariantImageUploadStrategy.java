package com.example.producr_service.application.strategy;

import com.example.producr_service.application.port.out.storage.StorageBucket;

// Chuẩn bị ảnh product variant với bucket và đường dẫn riêng.
public class ProductVariantImageUploadStrategy extends AbstractImageUploadStrategy {

    public ProductVariantImageUploadStrategy() {
        super(UploadPurpose.PRODUCT_VARIANT_IMAGE, StorageBucket.PRODUCT_VARIANTS, "Product");
    }
}
