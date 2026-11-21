package com.example.auth_service.adapter.mapper;

import com.example.auth_service.adapter.entity.ShopRequestEntity;
import com.example.auth_service.adapter.out.persistence.projection.AdminShopRequestProjection;
import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;
import com.example.auth_service.domain.model.ShopRequest;
import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface ShopRequestMapper {
    ShopRequest toDomain(ShopRequestEntity entity);

    ShopRequestEntity toEntity(ShopRequest shopRequest);

    default AdminShopRequestResponse toAdminResponse(AdminShopRequestProjection projection, ZoneId zoneId) {
        return new AdminShopRequestResponse(projection.getId(), projection.getUserId(), projection.getUserEmail(),
                projection.getUserFullName(), projection.getShopName(), projection.getShopDescription(),
                projection.getShopPhone(), projection.getProvinceCode(), projection.getProvinceName(),
                projection.getWard(), projection.getAddressDetail(), projection.getStatus(),
                projection.getRejectReason(), projection.getReviewedBy(),
                toOffset(projection.getCreatedAt(), zoneId), toOffset(projection.getReviewedAt(), zoneId));
    }

    private static OffsetDateTime toOffset(LocalDateTime value, ZoneId zoneId) {
        return value == null ? null : value.atZone(zoneId).toOffsetDateTime();
    }
}
