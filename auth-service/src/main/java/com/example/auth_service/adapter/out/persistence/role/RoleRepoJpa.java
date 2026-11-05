package com.example.auth_service.adapter.out.persistence.role;

import com.example.auth_service.domain.model.RoleEnum;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.adapter.entity.RoleEntity;


public interface RoleRepoJpa extends JpaRepository<RoleEntity, Long> {
    public Optional<RoleEntity> findByName(RoleEnum name);
}