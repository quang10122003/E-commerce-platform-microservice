package com.example.auth_service.adapter.mapper;


import org.mapstruct.Mapper;

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

    User toDomain(UserEntity entity);

    UserEntity toEntity(User user);

    UserDisplayInfoResponse toUserDisplayInfo(UserDisplayInfoProjection projection);

    ShopAddressView toShopAddress(ShopAddressProjection projection);

    ShopInfoResponse toShopInfo(ShopInfoProjection projection);


}
