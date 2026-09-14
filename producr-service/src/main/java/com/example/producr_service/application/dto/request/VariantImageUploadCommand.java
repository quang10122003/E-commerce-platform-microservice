package com.example.producr_service.application.dto.request;

// Command upload một ảnh và vị trí variant/image mà ảnh đó được gắn vào.
public record VariantImageUploadCommand(
        int variantIndex,
        int imageIndex,
        UploadFileCommand file
) {
}
