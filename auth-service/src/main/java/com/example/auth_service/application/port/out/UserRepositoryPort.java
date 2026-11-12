package com.example.auth_service.application.port.out;

import com.example.auth_service.application.DTO.repone.ShopStatusResponse;

import com.example.auth_service.domain.model.RoleEnum;

import com.example.auth_service.application.DTO.repone.ShopAddressView;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserAvatarResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.auth_service.application.DTO.repone.UserOverviewResponse;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByIdForUpdate(Long userId);

    Optional<User> findById(Long userId);

    List<ShopStatusResponse> findShopStatuses(
            List<Long> shopIds, Boolean locked);

    UserOverviewResponse getUserOverview();

    PageResponse<UserListResponse> findUsers(RoleEnum role, String keyword, Boolean locked, Boolean shopLocked, PageQuery pageQuery);

    List<UserDisplayInfoResponse> findUserDisplayInfosByIds(Collection<Long> userIds);

    List<UserAvatarResponse> findUserAvatarsByIds(Collection<Long> userIds);
    Optional<ShopAddressView> findShopAddressById(Long shopId);

    Optional<User> findByEmail(String email);

    List<ShopInfoResponse> findShopInfosByIds(Collection<Long> userIds);

    User save(User user);
}
