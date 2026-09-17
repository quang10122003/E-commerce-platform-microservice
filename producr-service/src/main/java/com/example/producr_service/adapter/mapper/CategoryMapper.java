package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.CategoryEntity;
import com.example.producr_service.domain.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    // Chuyển entity category thành domain model.
    Category toDomain(CategoryEntity entity);

    // Chuyển domain category thành entity lưu trữ.
    CategoryEntity toEntity(Category domain);
}
