package com.example.producr_service.adapter.out.persistence.Category;

import com.example.producr_service.adapter.mapper.CategoryMapper;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.domain.model.Category;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class CategoryRepoAdapter implements CategoryRepoPort {
    CategoryJpa categoryJpa;
    CategoryMapper categoryMapper;

    // Kiểm tra category có tồn tại theo mã định danh.
    @Override
    public boolean existsById(Long id) {
        return categoryJpa.existsById(id);
    }

    // Lấy category theo ID và chuyển entity thành domain model.
    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpa.findById(id).map(categoryMapper::toDomain);
    }

    // Lấy toàn bộ category và chuyển entity thành domain model.
    @Override
    public List<Category> getAllCategories() {
        return categoryJpa.findAll().stream().map(categoryMapper::toDomain).toList();
    }
}
