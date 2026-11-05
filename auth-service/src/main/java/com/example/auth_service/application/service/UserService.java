package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.repone.UserLockResponse;
import com.example.auth_service.application.DTO.repone.UserOverviewResponse;
import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.auth_service.application.port.in.GetUsersUseCase;
import com.example.auth_service.application.port.in.GetUserOverviewUseCase;
import com.example.auth_service.application.port.in.ToggleUserLockUseCase;
import com.example.auth_service.application.port.in.ToggleShopLockUseCase;
import com.example.auth_service.application.DTO.repone.ShopStatusResponse;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.application.port.out.cache.CachePort;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.domain.model.RoleEnum;
import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import com.example.common.security.AccessControlCacheKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class UserService implements GetUsersUseCase, GetUserOverviewUseCase, ToggleUserLockUseCase, ToggleShopLockUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final CachePort cachePort;

    @Override
    @Transactional
    public ShopStatusResponse toggleShopLock(Long userId) {
        User user = userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.SHOP_NOT_FOUND));
        user.toggleShopLock();
        userRepositoryPort.save(user);
        boolean locked = user.isShopLock();
        log.info("Thay đổi trạng thái khóa shop: shop_id={}, locked={}", userId, locked);
        return new ShopStatusResponse(userId, locked);
    }

    @Override
    @Transactional
    public UserLockResponse toggleUserLock(Long userId) {
        User user = userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.toggleUserLock();
        userRepositoryPort.save(user);
        boolean locked = user.isLocked();
        String key = AccessControlCacheKeys.userLocked(userId.toString());
        if (locked) {
            cachePort.set(key, "1");
        } else {
            cachePort.delete(key);
        }
        log.info("Thay đổi trạng thái khóa: user_id={}, locked={}", userId, locked);
        return new UserLockResponse(userId, locked);
    }

    @Override
    @Transactional(readOnly = true)
    public UserOverviewResponse getUserOverview() {
        return userRepositoryPort.getUserOverview();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserListResponse> getUsers(RoleEnum role, String keyword, Boolean locked, Boolean shopLocked, PageQuery pageQuery) {
        return userRepositoryPort.findUsers(role, keyword == null || keyword.isBlank() ? null : keyword.trim(),
                locked, shopLocked, pageQuery);
    }

}
