package com.example.auth_service.adapter.mapper;


import org.mapstruct.Mapper;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.domain.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Chuyển dữ liệu từ persistence sang domain
    User toDomain(UserEntity entity);

    // Chuyển dữ liệu từ domain sang persistence
    UserEntity toEntity(User user);

    // // Chuyển role từ persistence sang domain
    // com.example.auth_service.domain.model.Role toDomain(com.example.auth_service.entity.Role entity);

    // // Chuyển role từ domain sang persistence
    // com.example.auth_service.entity.Role toEntity(com.example.auth_service.domain.model.Role role);
}
