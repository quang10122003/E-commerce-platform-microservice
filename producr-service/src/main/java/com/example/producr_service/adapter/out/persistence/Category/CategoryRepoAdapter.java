package com.example.producr_service.adapter.out.persistence.Category;

import com.example.producr_service.application.port.out.CategoryRepoPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class CategoryRepoAdapter implements CategoryRepoPort {
    CategoryJpa categoryJpa;
    @Override
    public boolean existsById(Long id) {
        return categoryJpa.existsById(id);
    }
}
