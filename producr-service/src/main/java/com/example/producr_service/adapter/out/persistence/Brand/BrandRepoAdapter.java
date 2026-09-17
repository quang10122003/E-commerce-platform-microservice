package com.example.producr_service.adapter.out.persistence.Brand;

import com.example.producr_service.adapter.mapper.BrandMapper;
import com.example.producr_service.application.port.out.BrandRepositoryPort;
import com.example.producr_service.domain.model.Brand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class BrandRepoAdapter implements BrandRepositoryPort {
    BrandJpa brandJpa;
    BrandMapper brandMapper;

    @Override
    public boolean existsById(Long id) {
        return brandJpa.existsById(id);
    }

    // Lấy toàn bộ brand và chuyển entity thành domain model.
    @Override
    public List<Brand> getAllBrands() {
        return brandJpa.findAll().stream().map(brandMapper::toDomain).toList();
    }
}
