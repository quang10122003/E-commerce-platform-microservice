package com.example.producr_service.application.port.out.repo;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.AdminCategoryView;
import com.example.producr_service.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepoPort {
    boolean existsById(Long id);

    Optional<Category> findById(Long id);

    List<Category> getAllCategories();

    boolean existsByName(String name);

    PageResponse<AdminCategoryView> findAdminCategories(PageQuery pageQuery, String keyword);

    Category save(Category category);

    Optional<Category> findByIdForUpdate(Long id);

    boolean existsByNameAndIdNot(String name, Long id);

    Category update(Category category);

    void deleteById(Long id);
}
