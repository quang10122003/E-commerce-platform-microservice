package com.example.producr_service.application.port.in;

import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.AdminCategoryResponse;

public interface GetAdminCategoriesUseCase {

    PageResponse<AdminCategoryResponse> getAdminCategories(int page, int size, String keyword);
}
