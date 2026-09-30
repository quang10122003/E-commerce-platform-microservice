package com.example.producr_service.application.dto.command;

import com.example.producr_service.application.dto.request.UpdateProductData;

import java.util.List;

// Gom dữ liệu chỉnh sửa và các file mới thành một yêu cầu xử lý.
public record UpdateProductCommand(
        UpdateProductData productRequest,
        UploadFileCommand productImage,
        List<VariantImageUploadCommand> variantImages
) {}
