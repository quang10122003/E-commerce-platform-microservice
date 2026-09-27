package com.example.producr_service.application.dto.command;

import com.example.producr_service.application.dto.request.CreateProductData;

import java.util.List;

// Command đầu vào cho use case tạo product, gồm thông tin product và các ảnh cần upload.
public record CreateProductCommand(
        CreateProductData productRequest,
        UploadFileCommand productImage,
        List<VariantImageUploadCommand> variantImages
) {
}
