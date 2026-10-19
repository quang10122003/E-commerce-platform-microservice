package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepoPort {
    boolean existsById(Long id);

    Optional<Category> findById(Long id);

    List<Category> getAllCategories();
}
