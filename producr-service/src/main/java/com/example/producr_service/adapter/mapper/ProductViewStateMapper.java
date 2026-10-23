package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.UserProductViewStateEntity;
import com.example.producr_service.domain.model.ProductViewState;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface ProductViewStateMapper {
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "nextAllowedAt", expression = "java(entity.getNextAllowedAt().atZone(zone).toInstant())")
    ProductViewState toDomain(UserProductViewStateEntity entity, @Context ZoneId zone);
}
