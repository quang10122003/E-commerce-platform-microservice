package com.example.auth_service.application.port.out;

import com.example.auth_service.domain.model.RoleEnum;


import java.util.Optional;

import com.example.auth_service.domain.model.Role;

public interface RoleRepositoryPort {
    Optional<Role> findByName(RoleEnum name);

    Role save(Role role);
}