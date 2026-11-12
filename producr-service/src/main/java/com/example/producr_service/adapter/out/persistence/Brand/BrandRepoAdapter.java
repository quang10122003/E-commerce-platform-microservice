package com.example.producr_service.adapter.out.persistence.Brand;

import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.producr_service.adapter.entity.BrandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.producr_service.adapter.mapper.BrandMapper;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import com.example.producr_service.domain.model.Brand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Brand> findById(Long id) {
        return brandJpa.findById(id).map(brandMapper::toDomain);
    }

    @Override
    public List<Brand> findAllById(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return brandJpa.findAllById(ids).stream().map(brandMapper::toDomain).toList();
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandJpa.findAll().stream().map(brandMapper::toDomain).toList();
    }

    @Override
    public PageResponse<Brand> findAdminBrands(PageQuery pageQuery, String keyword) {
        PageRequest pageable = PageRequest.of(pageQuery.pageIndex(), pageQuery.size(), Sort.by(Sort.Direction.DESC, "id"));
        Page<BrandEntity> page = keyword == null
                ? brandJpa.findAll(pageable)
                : brandJpa.findByNameContainingIgnoreCase(keyword, pageable);
        return new PageResponse<>(page.getContent().stream().map(brandMapper::toDomain).toList(),
                page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public void deleteById(Long id) {
        brandJpa.deleteById(id);
    }
}
