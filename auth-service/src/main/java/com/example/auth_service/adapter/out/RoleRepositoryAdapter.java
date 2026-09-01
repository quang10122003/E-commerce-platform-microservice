package com.example.auth_service.adapter.out;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.auth_service.adapter.mapper.RoleMapper;
import com.example.auth_service.application.port.out.RoleRepositoryPort;
import com.example.auth_service.domain.model.Role;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * RoleRepositoryAdapter
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class RoleRepositoryAdapter implements RoleRepositoryPort  {
    RoleRepoJpa roleRepoJpa;
    RoleMapper roleMapper;

    @Override
    public Optional<Role> findByName(String name) {
        return  roleRepoJpa.findByName(name).map(roleMapper::toDomain);
    }

    @Override
    public Role save(Role role) {
        return roleMapper.toDomain(roleRepoJpa.save(roleMapper.toEntity(role)));
    }

    
}