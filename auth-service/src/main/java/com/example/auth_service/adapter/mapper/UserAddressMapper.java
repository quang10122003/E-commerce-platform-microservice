package com.example.auth_service.adapter.mapper;

import com.example.auth_service.adapter.entity.UserAddressEntity;
import com.example.auth_service.domain.model.UserAddress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {
    UserAddress toDomain(UserAddressEntity entity);

    UserAddressEntity toEntity(UserAddress address);
}
