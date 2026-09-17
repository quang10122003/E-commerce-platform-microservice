package com.example.producr_service.application.port.out;

import com.example.producr_service.domain.model.Brand;

import java.util.List;

public interface BrandRepositoryPort {
    boolean existsById(Long id);

    // Lấy toàn bộ brand từ nguồn dữ liệu.
    List<Brand> getAllBrands();
}
