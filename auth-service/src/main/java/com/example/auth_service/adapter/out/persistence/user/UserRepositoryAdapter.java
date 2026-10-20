package com.example.auth_service.adapter.out.persistence.user;

import com.example.auth_service.adapter.mapper.UserMapper;
import com.example.auth_service.application.DTO.repone.ShopAddressView;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.domain.until.RoleEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {
    UserRepoJpa userRepoJpa;
    UserMapper userMapper;

    @Override
    public List<UserDisplayInfoResponse> findUserDisplayInfosByIds(Collection<Long> userIds) {
        return userIds.isEmpty() ? List.of() : userRepoJpa.findUserDisplayInfosByIds(userIds).stream()
                .map(userMapper::toUserDisplayInfo).toList();
    }

    // Dùng vai trò shop hiện có để tránh công khai thông tin tài khoản thường.
    @Override
    public Optional<ShopAddressView> findShopAddressById(Long shopId) {
        return userRepoJpa.findShopAddressById(shopId, RoleEnum.SHOP.getName())
                .map(userMapper::toShopAddress);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepoJpa.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public List<ShopInfoResponse> findShopInfosByIds(Collection<Long> userIds) {
        return userIds.isEmpty() ? List.of() : userRepoJpa.findShopInfosByIds(userIds).stream()
                .map(userMapper::toShopInfo).toList();
    }

    @Override
    public User save(User user) {
        return userMapper.toDomain(userRepoJpa.save(userMapper.toEntity(user)));
    }

    
}
