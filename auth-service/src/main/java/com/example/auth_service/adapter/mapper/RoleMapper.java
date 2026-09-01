package com.example.auth_service.adapter.mapper;

import org.mapstruct.Mapper;

import com.example.auth_service.adapter.entity.RoleEntity;
import com.example.auth_service.domain.model.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
     // Chuyển dữ liệu từ persistence sang domain
    Role toDomain(RoleEntity entity);

    // Chuyển dữ liệu từ domain sang persistence
    RoleEntity toEntity(Role role);

}
