package com.example.auth_service.adapter.out.persistence.user;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.adapter.out.persistence.projection.ShopAddressProjection;
import com.example.auth_service.adapter.out.persistence.projection.ShopInfoProjection;
import com.example.auth_service.adapter.out.persistence.projection.UserDisplayInfoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepoJpa extends JpaRepository<UserEntity, Long> {
    // Danh sách chat không được làm lộ email hoặc thông tin liên hệ.
    @Query("select u.id as userId, u.fullName as fullName from UserEntity u where u.id in :userIds")
    List<UserDisplayInfoProjection> findUserDisplayInfosByIds(@Param("userIds") Collection<Long> userIds);
    @Query("""
            select u.id as shopId, u.shopName as shopName, u.shopAddress as shopAddress,
                   u.shopLogoUrl as shopLogoUrl, u.shopDescription as shopDescription,
                   u.shopCreatedAt as shopCreatedAt
            from UserEntity u join u.roles r
            where u.id = :shopId and r.name = :shopRole
            """)
    Optional<ShopAddressProjection> findShopAddressById(@Param("shopId") Long shopId,
                                               @Param("shopRole") String shopRole);

    public Optional<UserEntity> findByEmail(String email);

    @Query("select u.id as shopId, u.shopName as shopName, u.shopLogoUrl as shopLogoUrl from UserEntity u where u.id in :userIds")
    List<ShopInfoProjection> findShopInfosByIds(@Param("userIds") Collection<Long> userIds);
}
