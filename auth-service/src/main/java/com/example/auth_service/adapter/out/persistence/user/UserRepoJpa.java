package com.example.auth_service.adapter.out.persistence.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepoJpa extends JpaRepository<UserEntity, Long> {
    public Optional<UserEntity> findByEmail(String email);

    @Query("select new com.example.auth_service.application.DTO.repone.ShopInfoResponse(u.id, u.shopName) from UserEntity u where u.id in :userIds")
    List<ShopInfoResponse> findShopInfosByIds(@Param("userIds") Collection<Long> userIds);
}
