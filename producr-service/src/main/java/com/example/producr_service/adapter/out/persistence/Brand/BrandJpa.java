package com.example.producr_service.adapter.out.persistence.Brand;

import com.example.producr_service.adapter.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandJpa extends JpaRepository<BrandEntity,Long> {
}
