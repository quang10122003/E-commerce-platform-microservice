package com.example.producr_service.adapter.mapper;

import com.example.producr_service.adapter.entity.ProductEntity;
import com.example.producr_service.adapter.entity.UserProductBehaviorEntity;
import com.example.producr_service.adapter.out.persistence.projection.ProductBehaviorProjection;
import com.example.producr_service.domain.model.UserProductBehavior;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface ProductBehaviorMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventId", source = "behavior.eventId")
    @Mapping(target = "userId", source = "behavior.userId")
    @Mapping(target = "eventType", source = "behavior.eventType")
    @Mapping(target = "createdAt", source = "behavior.createdAt")
    @Mapping(target = "product", source = "product")
    UserProductBehaviorEntity toEntity(UserProductBehavior behavior, ProductEntity product, @Context ZoneId zone);

    default LocalDateTime toLocalDateTime(Instant instant, @Context ZoneId zone) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, zone);
    }

    default UserProductBehavior toDomain(
            ProductBehaviorProjection projection,
            ZoneId zone) {

        Instant createdAt = projection.getCreatedAt()
                .atZone(zone)
                .toInstant();

        return new UserProductBehavior(
                projection.getEventId(),
                projection.getUserId(),
                projection.getProductId(),
                projection.getEventType(),
                createdAt);
    }
}
