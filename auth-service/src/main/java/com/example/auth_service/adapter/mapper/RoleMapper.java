package com.example.auth_service.adapter.mapper;

import org.mapstruct.Mapper;

import com.example.auth_service.adapter.entity.RoleEntity;
import com.example.auth_service.domain.model.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toDomain(RoleEntity entity);

    RoleEntity toEntity(Role role);

}
