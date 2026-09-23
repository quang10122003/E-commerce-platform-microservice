package com.example.producr_service.application.port.out.repo;

import com.example.producr_service.domain.model.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepositoryPort {
    boolean existsById(Long id);

    // Lấy brand theo ID để bổ sung dữ liệu hiển thị cho product search document.
    Optional<Brand> findById(Long id);

    // Lấy toàn bộ brand từ nguồn dữ liệu.
    List<Brand> getAllBrands();
}
