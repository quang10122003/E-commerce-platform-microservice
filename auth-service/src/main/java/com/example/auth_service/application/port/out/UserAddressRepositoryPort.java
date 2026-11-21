package com.example.auth_service.application.port.out;

import com.example.auth_service.domain.model.UserAddress;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepositoryPort {
    // Địa chỉ mặc định đứng đầu, sau đó tới địa chỉ mới nhất.
    List<UserAddress> findByUserId(Long userId);

    Optional<UserAddress> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserId(Long userId);

    UserAddress save(UserAddress address);

    void clearDefault(Long userId);

    void deleteById(Long id);

    int deleteByUserId(Long userId);
}
