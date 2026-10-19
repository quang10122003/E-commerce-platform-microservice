package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.CategoryEntity;
import com.example.producr_service.domain.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toDomain(CategoryEntity entity);

    CategoryEntity toEntity(Category domain);
}
