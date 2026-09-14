package com.example.producr_service.adapter.out.persistence.Brand;

import com.example.producr_service.application.port.out.BrandRepositoryPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class BrandRepoAdapter implements BrandRepositoryPort {
    BrandJpa brandJpa;

    @Override
    public boolean existsById(Long id) {
        return brandJpa.existsById(id);
    }
}
