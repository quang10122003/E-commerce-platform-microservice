package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.BrandEntity;
import com.example.producr_service.domain.model.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    Brand toDomain(BrandEntity entity);

    BrandEntity toEntity(Brand domain);
}
