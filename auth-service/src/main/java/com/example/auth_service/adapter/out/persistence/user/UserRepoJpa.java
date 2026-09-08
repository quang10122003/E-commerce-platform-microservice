package com.example.auth_service.adapter.out.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.adapter.entity.UserEntity;

public interface UserRepoJpa extends JpaRepository<UserEntity, Long> {
    public Optional<UserEntity> findByEmail(String email);
}
