package com.example.producr_service.adapter.out.persistence.Brand;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.producr_service.adapter.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandJpa extends JpaRepository<BrandEntity,Long> {
    boolean existsByName(String name);

    // Containing của Spring Data tự escape ký tự % và _ trong từ khóa.
    Page<BrandEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
