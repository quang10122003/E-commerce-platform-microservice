package com.example.producr_service.adapter.out.persistence.Category;

import com.example.producr_service.adapter.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryJpa  extends JpaRepository<CategoryEntity,Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CategoryEntity c where c.id = :id")
    Optional<CategoryEntity> findByIdForUpdate(@Param("id") Long id);

    // Containing của Spring Data tự escape ký tự % và _ trong từ khóa.
    Page<CategoryEntity> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
