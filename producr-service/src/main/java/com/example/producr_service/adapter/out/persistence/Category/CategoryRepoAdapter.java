package com.example.producr_service.adapter.out.persistence.Category;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.adapter.entity.CategoryEntity;
import com.example.producr_service.adapter.mapper.CategoryMapper;
import com.example.producr_service.application.dto.response.AdminCategoryView;
import com.example.producr_service.application.port.out.repo.CategoryRepoPort;
import com.example.producr_service.application.port.out.storage.StorageBucket;
import com.example.producr_service.domain.model.Category;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class CategoryRepoAdapter implements CategoryRepoPort {
    CategoryJpa categoryJpa;
    CategoryMapper categoryMapper;
    Clock clock;

    @Override
    public boolean existsById(Long id) {
        return categoryJpa.existsById(id);
    }

    @Override
    public Optional<Category> findById(Long id) {
        return categoryJpa.findById(id).map(categoryMapper::toDomain);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryJpa.findAll().stream().map(categoryMapper::toDomain).toList();
    }

    @Override
    public boolean existsByName(String name) {
        return categoryJpa.existsByName(name);
    }

    @Override
    public PageResponse<AdminCategoryView> findAdminCategories(PageQuery pageQuery, String keyword) {
        PageRequest pageable = PageRequest.of(pageQuery.pageIndex(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "id"));
        Page<CategoryEntity> page = keyword == null
                ? categoryJpa.findAll(pageable)
                : categoryJpa.findByNameContainingIgnoreCase(keyword, pageable);
        return new PageResponse<>(
                page.getContent().stream()
                        .map(entity -> new AdminCategoryView(entity.getId(), entity.getName(), entity.getImageUrl(),
                                entity.getCreatedAt(), entity.getUpdatedAt()))
                        .toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public Optional<Category> findByIdForUpdate(Long id) {
        return categoryJpa.findByIdForUpdate(id).map(categoryMapper::toDomain);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return categoryJpa.existsByNameAndIdNot(name, id);
    }

    @Override
    public Category update(Category category) {
        CategoryEntity entity = categoryJpa.findById(category.getId()).orElseThrow();
        entity.setName(category.getName());
        entity.setImageUrl(category.getImageUrl());
        entity.setObjectPath(category.getObjectPath());
        entity.setStorageBucket(category.getObjectPath() == null ? null : StorageBucket.CATEGORY);
        entity.setUpdatedAt(LocalDateTime.now(clock));
        return categoryMapper.toDomain(categoryJpa.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        categoryJpa.deleteById(id);
    }

    // Gán created_at tường minh vì JPA chèn NULL đè DEFAULT của cột.
    @Override
    public Category save(Category category) {
        CategoryEntity entity = categoryMapper.toEntity(category);
        LocalDateTime now = LocalDateTime.now(clock);
        entity.setStorageBucket(category.getObjectPath() == null ? null : StorageBucket.CATEGORY);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return categoryMapper.toDomain(categoryJpa.save(entity));
    }
}
