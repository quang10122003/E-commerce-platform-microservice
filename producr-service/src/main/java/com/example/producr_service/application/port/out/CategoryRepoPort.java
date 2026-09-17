package com.example.producr_service.application.port.out;

import com.example.producr_service.domain.model.Category;

import java.util.List;

public interface CategoryRepoPort {
    boolean existsById(Long id);

    // Lấy toàn bộ category từ nguồn dữ liệu.
    List<Category> getAllCategories();
}
