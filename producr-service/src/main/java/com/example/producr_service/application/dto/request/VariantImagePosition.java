package com.example.producr_service.application.dto.request;

// Đại diện cho vị trí duy nhất của một ảnh trong một variant.
public record VariantImagePosition(
        int variantIndex,
        int imageIndex
) {
}
