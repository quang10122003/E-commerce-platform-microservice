package com.example.auth_service.application.port.out;


import java.util.Optional;

import com.example.auth_service.domain.model.Role;

public interface RoleRepositoryPort {
    Optional<Role> findByName(String name);

    Role save(Role role);
}