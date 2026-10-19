package com.example.auth_service.adapter.out.persistence.user;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.application.DTO.repone.ShopAddressView;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepoJpa extends JpaRepository<UserEntity, Long> {
    // Danh sách chat không được làm lộ email hoặc thông tin liên hệ.
    @Query("select new com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse(u.id, u.fullName) from UserEntity u where u.id in :userIds")
    List<UserDisplayInfoResponse> findUserDisplayInfosByIds(@Param("userIds") Collection<Long> userIds);
    @Query("""
            select new com.example.auth_service.application.DTO.repone.ShopAddressView(
                u.id, u.shopName, u.shopAddress)
            from UserEntity u join u.roles r
            where u.id = :shopId and r.name = :shopRole
            """)
    Optional<ShopAddressView> findShopAddressById(@Param("shopId") Long shopId,
                                               @Param("shopRole") String shopRole);

    public Optional<UserEntity> findByEmail(String email);

    @Query("select new com.example.auth_service.application.DTO.repone.ShopInfoResponse(u.id, u.shopName, u.shopLogoUrl) from UserEntity u where u.id in :userIds")
    List<ShopInfoResponse> findShopInfosByIds(@Param("userIds") Collection<Long> userIds);
}
