package com.example.auth_service.adapter.mapper;


import org.mapstruct.Mapper;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User user);


}
