package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.command.UploadFileCommand;
import com.example.producr_service.application.dto.response.CategoryResponse;

public interface CreateCategoryUseCase {

    CategoryResponse createCategory(String name, UploadFileCommand image);
}
