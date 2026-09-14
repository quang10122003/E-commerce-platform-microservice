package com.example.producr_service.application.dto.request;

import java.util.List;

// Command đầu vào cho use case tạo product, gồm thông tin product và các ảnh cần upload.
public record CreateProductCommand(
        CreateProductRequest productRequest,
        UploadFileCommand productImage,
        List<VariantImageUploadCommand> variantImages
) {
}
