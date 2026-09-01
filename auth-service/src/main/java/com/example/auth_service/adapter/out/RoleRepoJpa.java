package com.example.auth_service.adapter.out;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.adapter.entity.RoleEntity;


/**
 * RoleRepoJpa
 */
public interface RoleRepoJpa extends JpaRepository<RoleEntity, Long> {
    public Optional<RoleEntity> findByName(String name);
}