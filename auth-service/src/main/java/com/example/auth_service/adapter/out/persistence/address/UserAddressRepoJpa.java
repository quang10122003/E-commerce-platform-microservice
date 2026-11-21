package com.example.auth_service.adapter.out.persistence.address;

import com.example.auth_service.adapter.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepoJpa extends JpaRepository<UserAddressEntity, Long> {
    List<UserAddressEntity> findByUserIdOrderByDefaultAddressDescIdDesc(Long userId);

    Optional<UserAddressEntity> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserId(Long userId);

    // flushAutomatically để lệnh ghi đang chờ chạy trước, clearAutomatically để entity đã tải không giữ cờ mặc định cũ.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update UserAddressEntity a set a.defaultAddress = false where a.userId = :userId and a.defaultAddress = true")
    int clearDefault(@Param("userId") Long userId);

    @Modifying
    @Query("delete from UserAddressEntity a where a.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
