package com.example.auth_service.application.port.out;


import java.util.Optional;

import com.example.auth_service.domain.model.User;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);

    User save(User user);
}