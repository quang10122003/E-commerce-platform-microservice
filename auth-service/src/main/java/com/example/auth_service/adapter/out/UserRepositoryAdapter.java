package com.example.auth_service.adapter.out;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.example.auth_service.adapter.mapper.UserMapper;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.User;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Adapter chuyển đổi giữa domain và persistence cho User.
 */
@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {
    UserRepoJpa userRepoJpa;
    UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepoJpa.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        // Lưu domain user rồi đổi ngược về domain để trả ra cho application
        return userMapper.toDomain(userRepoJpa.save(userMapper.toEntity(user)));
    }

    
}
