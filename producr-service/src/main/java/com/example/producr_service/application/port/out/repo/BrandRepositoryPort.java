package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepositoryPort {
    boolean existsById(Long id);

    Optional<Brand> findById(Long id);

    List<Brand> findAllById(List<Long> ids);

    List<Brand> getAllBrands();
}
