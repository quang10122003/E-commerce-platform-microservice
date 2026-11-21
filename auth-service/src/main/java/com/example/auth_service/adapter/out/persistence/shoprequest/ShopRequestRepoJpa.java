package com.example.auth_service.adapter.out.persistence.shoprequest;

import com.example.auth_service.adapter.entity.ShopRequestEntity;
import com.example.auth_service.adapter.out.persistence.projection.AdminShopRequestProjection;
import com.example.auth_service.domain.model.ShopRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShopRequestRepoJpa extends JpaRepository<ShopRequestEntity, Long> {
    String ADMIN_VIEW_SELECT = """
            select r.id as id, r.userId as userId, u.email as userEmail, u.fullName as userFullName,
                   r.shopName as shopName, r.shopDescription as shopDescription, r.shopPhone as shopPhone,
                   r.provinceCode as provinceCode, r.provinceName as provinceName, r.ward as ward,
                   r.addressDetail as addressDetail, r.status as status, r.rejectReason as rejectReason,
                   r.reviewedBy as reviewedBy, r.createdAt as createdAt, r.reviewedAt as reviewedAt
            from ShopRequestEntity r join UserEntity u on u.id = r.userId
            """;
    String ADMIN_FILTER = """
            where (:status is null or r.status = :status)
              and (:keyword is null or lower(r.shopName) like :keyword escape '!'
                   or lower(u.email) like :keyword escape '!' or lower(u.fullName) like :keyword escape '!')
            """;

    Optional<ShopRequestEntity> findFirstByUserIdOrderByIdDesc(Long userId);

    Optional<ShopRequestEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndStatus(Long userId, ShopRequestStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ShopRequestEntity r where r.id = :id")
    Optional<ShopRequestEntity> findByIdForUpdate(@Param("id") Long id);

    @Query(value = ADMIN_VIEW_SELECT + ADMIN_FILTER + " order by r.id desc",
            countQuery = "select count(r.id) from ShopRequestEntity r join UserEntity u on u.id = r.userId " + ADMIN_FILTER)
    Page<AdminShopRequestProjection> findAdminPage(@Param("status") ShopRequestStatus status,
                                                   @Param("keyword") String keyword, Pageable pageable);

    @Query(ADMIN_VIEW_SELECT + " where r.id = :id")
    Optional<AdminShopRequestProjection> findAdminView(@Param("id") Long id);
}
