package com.example.producr_service.application.service;


import com.example.producr_service.application.dto.response.CategoryResponse;
import com.example.producr_service.application.port.in.GetCategoriesUseCase;
import com.example.producr_service.application.port.out.CategoryRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class CategoryService implements GetCategoriesUseCase {
    private final CategoryRepoPort categoryRepoPort;

    @Transactional(readOnly = true)
    @Override
    public List<CategoryResponse> getCategories() {
        // Lấy domain từ repository rồi chuyển thành response ở application layer.
        return categoryRepoPort.getAllCategories()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
