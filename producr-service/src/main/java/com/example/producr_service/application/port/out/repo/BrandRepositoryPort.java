package com.example.producr_service.application.port.out.repo;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.domain.model.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepositoryPort {
    boolean existsById(Long id);

    boolean existsByName(String name);

    Optional<Brand> findById(Long id);

    List<Brand> findAllById(List<Long> ids);

    List<Brand> getAllBrands();

    PageResponse<Brand> findAdminBrands(PageQuery pageQuery, String keyword);

    void deleteById(Long id);

    Brand save(Brand brand);
}
