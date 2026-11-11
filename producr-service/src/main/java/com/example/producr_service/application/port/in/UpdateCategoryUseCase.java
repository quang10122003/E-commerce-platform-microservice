package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.dto.response.CategoryResponse;

public interface UpdateCategoryUseCase {

    // image null nghĩa là giữ ảnh hiện tại.
    CategoryResponse updateCategory(Long categoryId, String name, UploadFileCommand image);
}
