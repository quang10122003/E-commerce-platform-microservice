package com.example.auth_service.application.port.out;

import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;
import com.example.auth_service.domain.model.ShopRequest;
import com.example.auth_service.domain.model.ShopRequestStatus;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;

import java.time.ZoneId;
import java.util.Optional;

public interface ShopRequestRepositoryPort {
    ShopRequest save(ShopRequest shopRequest);

    Optional<ShopRequest> findLatestByUserId(Long userId);

    Optional<ShopRequest> findByIdAndUserId(Long id, Long userId);

    boolean existsPendingByUserId(Long userId);

    Optional<ShopRequest> findById(Long id);

    Optional<ShopRequest> findByIdForUpdate(Long id);

    // keyword khớp tên shop, email hoặc họ tên người nộp; mới nhất trước.
    PageResponse<AdminShopRequestResponse> findAdminPage(ShopRequestStatus status, String keyword, PageQuery pageQuery,
                                                         ZoneId zoneId);

    Optional<AdminShopRequestResponse> findAdminView(Long id, ZoneId zoneId);
}
