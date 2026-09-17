package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.CategoryResponse;

import java.util.List;

public interface GetCategoriesUseCase {

    // Lấy danh sách category để cung cấp cho API.
    List<CategoryResponse> getCategories();
}
