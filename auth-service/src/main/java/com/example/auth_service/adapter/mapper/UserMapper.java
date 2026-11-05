package com.example.auth_service.adapter.mapper;

import com.example.auth_service.adapter.out.persistence.projection.ShopStatusProjection;
import com.example.auth_service.application.DTO.repone.ShopStatusResponse;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import com.example.auth_service.domain.model.RoleEnum;
import com.example.auth_service.adapter.out.persistence.projection.UserListProjection;
import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.auth_service.adapter.out.persistence.projection.UserOverviewProjection;
import com.example.auth_service.application.DTO.repone.UserOverviewResponse;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.adapter.out.persistence.projection.ShopAddressProjection;
import com.example.auth_service.adapter.out.persistence.projection.ShopInfoProjection;
import com.example.auth_service.adapter.out.persistence.projection.UserDisplayInfoProjection;
import com.example.auth_service.application.DTO.repone.ShopAddressView;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    ShopStatusResponse toShopStatus(
            ShopStatusProjection projection);

    UserOverviewResponse toUserOverview(UserOverviewProjection projection);

    @Mapping(target = "roles", source = "roles")
    UserListResponse toUserList(UserListProjection projection, List<RoleEnum> roles);

    User toDomain(UserEntity entity);

    @Mapping(target = "isShopLock", source = "shopLock")
    @Mapping(target = "isLocked", source = "locked")
    UserEntity toEntity(User user);

    UserDisplayInfoResponse toUserDisplayInfo(UserDisplayInfoProjection projection);

    ShopAddressView toShopAddress(ShopAddressProjection projection);

    ShopInfoResponse toShopInfo(ShopInfoProjection projection);


}
