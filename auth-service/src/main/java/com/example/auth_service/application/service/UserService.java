package com.example.auth_service.application.service;

import com.example.auth_service.application.port.out.storage.StorageBucket;
import com.example.auth_service.application.port.out.storage.StoredFile;
import com.example.auth_service.application.DTO.repone.UserLockResponse;
import com.example.auth_service.application.DTO.repone.UserOverviewResponse;
import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.auth_service.application.DTO.event.UserDeletedEventDto;
import com.example.auth_service.application.constant.Constant;
import com.example.auth_service.application.port.in.DeleteUserUseCase;
import com.example.auth_service.application.port.in.GetUsersUseCase;
import com.example.auth_service.application.port.out.OrderActivityPort;
import com.example.auth_service.application.port.out.OutboxEventPort;
import com.example.auth_service.domain.model.OutboxEvent;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class UserService implements GetUsersUseCase, GetUserOverviewUseCase, ToggleUserLockUseCase, ToggleShopLockUseCase,
        DeleteUserUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final CachePort cachePort;
    private final OrderActivityPort orderActivityPort;
    private final OutboxEventPort outboxEventPort;
    private final Clock clock;

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

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        // Giữ vị trí logo trước khi ẩn danh để dọn file sau commit.
        String logoObjectPath = user.getShopLogoObjectPath();
        String logoUrl = user.getShopLogoUrl();
        String avatarObjectPath = user.getAvatarObjectPath();
        String avatarUrl = user.getAvatarUrl();
        // Domain chặn admin và tài khoản đã xóa trước khi gọi sang order-service.
        user.anonymize(clock.instant());
        if (orderActivityPort.hasActiveOrders(userId)) {
            throw new BusinessException(AuthError.USER_HAS_ACTIVE_ORDERS);
        }
        userRepositoryPort.save(user);
        // Gateway đọc key này để chặn ngay access token còn hạn của tài khoản đã xóa.
        cachePort.set(AccessControlCacheKeys.userLocked(userId.toString()), "1");
        if (logoObjectPath != null) {
            ShopProfileService.saveDeleteFilesEvent(userId,
                    new StoredFile(StorageBucket.SHOP_LOGO, logoObjectPath, logoUrl), outboxEventPort, clock);
        }
        if (avatarObjectPath != null) {
            ShopProfileService.saveDeleteFilesEvent(userId,
                    new StoredFile(StorageBucket.USER_AVATAR, avatarObjectPath, avatarUrl), outboxEventPort, clock);
        }
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now(clock);
        outboxEventPort.save(new OutboxEvent(eventId, Constant.USER, String.valueOf(userId),
                UserDeletedEventDto.EVENT_TYPE,
                new UserDeletedEventDto(eventId, UserDeletedEventDto.EVENT_TYPE, userId, now), now));
        log.info("Xóa mềm tài khoản: user_id={}", userId);
    }
}
