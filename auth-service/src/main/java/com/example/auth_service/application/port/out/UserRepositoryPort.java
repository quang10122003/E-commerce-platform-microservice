package com.example.auth_service.application.port.out;

import com.example.auth_service.application.DTO.repone.ShopAddressView;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import com.example.auth_service.domain.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    List<UserDisplayInfoResponse> findUserDisplayInfosByIds(Collection<Long> userIds);
    Optional<ShopAddressView> findShopAddressById(Long shopId);

    Optional<User> findByEmail(String email);

    List<ShopInfoResponse> findShopInfosByIds(Collection<Long> userIds);

    User save(User user);
}
