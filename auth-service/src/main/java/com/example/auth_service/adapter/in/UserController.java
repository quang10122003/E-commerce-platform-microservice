package com.example.auth_service.adapter.in;

import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.auth_service.application.DTO.repone.UserLockResponse;
import com.example.auth_service.application.port.in.ToggleUserLockUseCase;
import com.example.auth_service.application.port.in.ToggleShopLockUseCase;
import com.example.auth_service.application.DTO.repone.ShopStatusResponse;
import com.example.auth_service.application.port.in.GetUsersUseCase;
import com.example.auth_service.application.port.in.GetUserOverviewUseCase;
import com.example.auth_service.application.DTO.repone.UserOverviewResponse;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import com.example.auth_service.domain.model.RoleEnum;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final GetUsersUseCase getUsersUseCase;
    private final GetUserOverviewUseCase getUserOverviewUseCase;
    private final ToggleUserLockUseCase toggleUserLockUseCase;
    private final ToggleShopLockUseCase toggleShopLockUseCase;

    @PostMapping("/{userId}/shop/toggle-lock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ShopStatusResponse toggleShopLock(@PathVariable @Positive Long userId) {
        return toggleShopLockUseCase.toggleShopLock(userId);
    }

    @PostMapping("/{userId}/toggle-lock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserLockResponse toggleUserLock(@PathVariable @Positive Long userId) {
        return toggleUserLockUseCase.toggleUserLock(userId);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserOverviewResponse getUserOverview() {
        return getUserOverviewUseCase.getUserOverview();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PageResponse<UserListResponse> getUsers(
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) @Size(max = 255) String keyword,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(required = false) Boolean shopLocked,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return getUsersUseCase.getUsers(role, keyword, locked, shopLocked, new PageQuery(page, size));
    }
}
