package com.example.producr_service.adapter.out.persistence.Product;

import com.example.producr_service.adapter.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpa extends JpaRepository<ProductEntity,Long> {

}
