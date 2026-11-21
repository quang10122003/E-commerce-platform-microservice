package com.example.auth_service.adapter.out.persistence.address;

import com.example.auth_service.adapter.mapper.UserAddressMapper;
import com.example.auth_service.application.port.out.UserAddressRepositoryPort;
import com.example.auth_service.domain.model.UserAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAddressRepositoryAdapter implements UserAddressRepositoryPort {
    private final UserAddressRepoJpa userAddressRepoJpa;
    private final UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> findByUserId(Long userId) {
        return userAddressRepoJpa.findByUserIdOrderByDefaultAddressDescIdDesc(userId).stream()
                .map(userAddressMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserAddress> findByIdAndUserId(Long id, Long userId) {
        return userAddressRepoJpa.findByIdAndUserId(id, userId).map(userAddressMapper::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return userAddressRepoJpa.existsByUserId(userId);
    }

    @Override
    public UserAddress save(UserAddress address) {
        return userAddressMapper.toDomain(userAddressRepoJpa.save(userAddressMapper.toEntity(address)));
    }

    @Override
    public void clearDefault(Long userId) {
        userAddressRepoJpa.clearDefault(userId);
    }

    @Override
    public void deleteById(Long id) {
        userAddressRepoJpa.deleteById(id);
    }

    @Override
    public int deleteByUserId(Long userId) {
        return userAddressRepoJpa.deleteByUserId(userId);
    }
}
