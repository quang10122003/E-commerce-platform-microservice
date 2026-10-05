package com.example.auth_service.application.port.out;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.domain.model.User;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);

    List<ShopInfoResponse> findShopInfosByIds(Collection<Long> userIds);

    User save(User user);
}