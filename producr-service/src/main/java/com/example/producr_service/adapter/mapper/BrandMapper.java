package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.BrandEntity;
import com.example.producr_service.domain.model.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    // Chuyển entity brand thành domain model.
    Brand toDomain(BrandEntity entity);

    // Chuyển domain model thành entity brand.
    BrandEntity toEntity(Brand domain);
}
