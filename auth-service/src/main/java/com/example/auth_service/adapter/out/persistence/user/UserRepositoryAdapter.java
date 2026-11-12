package com.example.auth_service.adapter.out.persistence.user;

import com.example.auth_service.application.DTO.repone.ShopStatusResponse;

import com.example.auth_service.adapter.mapper.UserMapper;
import com.example.auth_service.application.DTO.repone.ShopAddressView;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserAvatarResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.domain.model.RoleEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.auth_service.adapter.out.persistence.projection.UserListProjection;
import com.example.auth_service.adapter.out.persistence.projection.UserRoleProjection;
import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.auth_service.application.DTO.repone.UserOverviewResponse;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {
    UserRepoJpa userRepoJpa;
    UserMapper userMapper;

    @Override
    public Optional<User> findByIdForUpdate(Long userId) {
        return userRepoJpa.findByIdForUpdate(userId).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userRepoJpa.findById(userId).map(userMapper::toDomain);
    }

    @Override
    public List<ShopStatusResponse> findShopStatuses(
            List<Long> shopIds, Boolean locked) {
        boolean filterIds = shopIds != null;
        if (filterIds && shopIds.isEmpty()) {
            return List.of();
        }
        return userRepoJpa.findShopStatuses(filterIds ? shopIds : List.of(-1L), filterIds,
                locked, RoleEnum.ROLE_SHOP).stream().map(userMapper::toShopStatus).toList();
    }

    @Override
    public UserOverviewResponse getUserOverview() {
        return userMapper.toUserOverview(userRepoJpa.getUserOverview(RoleEnum.ROLE_SHOP));
    }

    @Override
    public PageResponse<UserListResponse> findUsers(RoleEnum role, String keyword, Boolean locked, Boolean shopLocked, PageQuery pageQuery) {
        String pattern = keyword == null ? null : "%" + keyword.toLowerCase(Locale.ROOT)
                .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        Page<UserListProjection> page = userRepoJpa.findUsers(role, pattern, locked, shopLocked,
                PageRequest.of(pageQuery.pageIndex(), pageQuery.size()));
        List<Long> userIds = page.getContent().stream().map(UserListProjection::getId).toList();
        Map<Long, List<RoleEnum>> rolesByUser = userIds.isEmpty() ? Map.of()
                : userRepoJpa.findUserRolesByIds(userIds).stream().collect(Collectors.groupingBy(
                        UserRoleProjection::getUserId,
                        Collectors.mapping(UserRoleProjection::getRoleName, Collectors.toList())));
        List<UserListResponse> users = page.getContent().stream()
                .map(user -> userMapper.toUserList(user, rolesByUser.getOrDefault(user.getId(), List.of())))
                .toList();
        return new PageResponse<>(users, pageQuery.page(), pageQuery.size(),
                page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public List<UserDisplayInfoResponse> findUserDisplayInfosByIds(Collection<Long> userIds) {
        return userIds.isEmpty() ? List.of() : userRepoJpa.findUserDisplayInfosByIds(userIds).stream()
                .map(userMapper::toUserDisplayInfo).toList();
    }

    @Override
    public List<UserAvatarResponse> findUserAvatarsByIds(Collection<Long> userIds) {
        return userIds.isEmpty() ? List.of() : userRepoJpa.findUserAvatarsByIds(userIds).stream()
                .map(userMapper::toUserAvatar).toList();
    }

    // Dùng vai trò shop hiện có để tránh công khai thông tin tài khoản thường.
    @Override
    public Optional<ShopAddressView> findShopAddressById(Long shopId) {
        return userRepoJpa.findShopAddressById(shopId, RoleEnum.ROLE_SHOP)
                .map(userMapper::toShopAddress);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepoJpa.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public List<ShopInfoResponse> findShopInfosByIds(Collection<Long> userIds) {
        return userIds.isEmpty() ? List.of() : userRepoJpa.findShopInfosByIds(userIds).stream()
                .map(userMapper::toShopInfo).toList();
    }

    @Override
    public User save(User user) {
        return userMapper.toDomain(userRepoJpa.save(userMapper.toEntity(user)));
    }

    
}
