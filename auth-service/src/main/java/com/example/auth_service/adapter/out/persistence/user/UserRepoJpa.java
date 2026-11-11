package com.example.auth_service.adapter.out.persistence.user;

import com.example.auth_service.adapter.out.persistence.projection.ShopStatusProjection;

import com.example.auth_service.domain.model.RoleEnum;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.adapter.out.persistence.projection.ShopAddressProjection;
import com.example.auth_service.adapter.out.persistence.projection.ShopInfoProjection;
import com.example.auth_service.adapter.out.persistence.projection.UserDisplayInfoProjection;
import com.example.auth_service.adapter.out.persistence.projection.UserListProjection;
import com.example.auth_service.adapter.out.persistence.projection.UserRoleProjection;
import com.example.auth_service.adapter.out.persistence.projection.UserOverviewProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepoJpa extends JpaRepository<UserEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.id = :userId")
    Optional<UserEntity> findByIdForUpdate(@Param("userId") Long userId);

    @Query("""
            select count(distinct u.id) as totalAccounts,
                   count(distinct case when r.id is not null then u.id end) as totalShops,
                   count(distinct case when r.id is not null and u.isShopLock = true then u.id end) as inactiveShops,
                   count(distinct case when u.isLocked = true then u.id end) as lockedAccounts,
                   count(distinct case when u.isLocked = false then u.id end) as activeUsers
            from UserEntity u left join u.roles r on r.name = :shopRole
            """)
    UserOverviewProjection getUserOverview(@Param("shopRole") RoleEnum shopRole);

    @Query(value = """
            select u.id as id, u.email as email, u.fullName as fullName,
                   u.isLocked as locked, u.createdAt as createdAt,
                   u.shopName as shopName, u.shopLogoUrl as shopLogoUrl,
                   u.shopDescription as shopDescription, u.shopPhone as shopPhone,
                   u.shopAddress as shopAddress, u.shopCreatedAt as shopCreatedAt,
                   u.isShopLock as shopLocked, u.deletedAt as deletedAt
            from UserEntity u
            where (:role is null or exists (select r.id from u.roles r where r.name = :role))
              and (:keyword is null or lower(u.fullName) like :keyword escape '!'
                   or lower(u.email) like :keyword escape '!')
              and (:locked is null or u.isLocked = :locked)
              and (:shopLocked is null or u.isShopLock = :shopLocked)
            order by u.id desc
            """, countQuery = """
            select count(u.id) from UserEntity u
            where (:role is null or exists (select r.id from u.roles r where r.name = :role))
              and (:keyword is null or lower(u.fullName) like :keyword escape '!'
                   or lower(u.email) like :keyword escape '!')
              and (:locked is null or u.isLocked = :locked)
              and (:shopLocked is null or u.isShopLock = :shopLocked)
            """)
    Page<UserListProjection> findUsers(@Param("role") RoleEnum role, @Param("keyword") String keyword,
                                       @Param("locked") Boolean locked, @Param("shopLocked") Boolean shopLocked, Pageable pageable);

    @Query("""
            select u.id as userId, r.name as roleName
            from UserEntity u join u.roles r where u.id in :userIds order by r.name
            """)
    List<UserRoleProjection> findUserRolesByIds(@Param("userIds") Collection<Long> userIds);

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
                                               @Param("shopRole") RoleEnum shopRole);

    @Query("""
            select u.id as shopId, u.isShopLock as locked
            from UserEntity u join u.roles r
            where r.name = :shopRole
              and (:filterIds = false or u.id in :shopIds)
              and (:locked is null or u.isShopLock = :locked)
            """)
    List<ShopStatusProjection> findShopStatuses(
            @Param("shopIds") List<Long> shopIds, @Param("filterIds") boolean filterIds,
            @Param("locked") Boolean locked, @Param("shopRole") RoleEnum shopRole);

    public Optional<UserEntity> findByEmail(String email);

    @Query("select u.id as shopId, u.shopName as shopName, u.shopLogoUrl as shopLogoUrl from UserEntity u where u.id in :userIds")
    List<ShopInfoProjection> findShopInfosByIds(@Param("userIds") Collection<Long> userIds);
}
