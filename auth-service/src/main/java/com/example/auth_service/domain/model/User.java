package com.example.auth_service.domain.model;

import java.time.Instant;
import java.util.Set;

import com.example.auth_service.application.error.AuthError;
import com.example.common.exception.BusinessException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    private String password;
    private String fullName;
    private String shopName;
    private String shopLogoUrl;
    private String shopDescription;
    private String shopPhone;
    private String shopAddress;
    private boolean isShopLock;
    private Set<Role> roles;
    private boolean locked;
    private Instant createdAt;
    private Instant updatedAt;

    // Tạo User mới trong nghiệp vụ đăng ký, để ID và thời gian do persistence sinh.
    public static User create(
            String email,
            String password,
            String fullName,
            Set<Role> roles) {
        return User.builder()
                .email(email)
                .password(password)
                .fullName(fullName)
                .shopName(null)
                .shopLogoUrl(null)
                .shopDescription(null)
                .shopPhone(null)
                .shopAddress(null)
                .isShopLock(false)
                .roles(roles)
                .locked(false)
                .build();
    }

    public boolean canLogin() {
        return !locked;
    }

    // Kiểm tra tài khoản có bị khóa hay không
    public void assertUserNotLocked() {
        if (this.isLocked()) {
            throw new BusinessException(AuthError.USER_LOCKED);
        }
    }
    
}
