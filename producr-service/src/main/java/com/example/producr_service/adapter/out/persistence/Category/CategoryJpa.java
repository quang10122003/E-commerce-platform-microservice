package com.example.producr_service.adapter.out.persistence.Category;

import com.example.producr_service.adapter.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpa  extends JpaRepository<CategoryEntity,Long> {
}
