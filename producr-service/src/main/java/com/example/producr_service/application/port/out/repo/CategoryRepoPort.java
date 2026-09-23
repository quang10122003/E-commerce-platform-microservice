package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepoPort {
    boolean existsById(Long id);

    // Lấy category theo ID để bổ sung dữ liệu hiển thị cho product search document.
    Optional<Category> findById(Long id);

    // Lấy toàn bộ category từ nguồn dữ liệu.
    List<Category> getAllCategories();
}
