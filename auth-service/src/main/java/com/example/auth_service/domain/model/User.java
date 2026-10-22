package com.example.auth_service.domain.model;

import java.time.Instant;
import java.util.Set;

import com.example.auth_service.domain.error.DomainAuthError;
import com.example.common.exception.BusinessException;

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
    private Instant shopCreatedAt;
    private boolean shopLock;
    private Set<Role> roles;
    private boolean locked;
    private Instant createdAt;
    private Instant updatedAt;

    public User(Long id, String email, String password, String fullName, String shopName,
                String shopLogoUrl, String shopDescription, String shopPhone, String shopAddress,
                Instant shopCreatedAt, boolean shopLock, Set<Role> roles, boolean locked,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.shopName = shopName;
        this.shopLogoUrl = shopLogoUrl;
        this.shopDescription = shopDescription;
        this.shopPhone = shopPhone;
        this.shopAddress = shopAddress;
        this.shopCreatedAt = shopCreatedAt;
        this.shopLock = shopLock;
        this.roles = roles;
        this.locked = locked;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ID và thời gian do persistence sinh, không tự gán khi đăng ký.
    public static User create(
            String email,
            String password,
            String fullName,
            Set<Role> roles) {
        return new User(null, email, password, fullName, null, null, null, null, null,
                null, false, roles, false, null, null);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopLogoUrl() {
        return shopLogoUrl;
    }

    public String getShopDescription() {
        return shopDescription;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public Instant getShopCreatedAt() {
        return shopCreatedAt;
    }

    public boolean isShopLock() {
        return shopLock;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public boolean isLocked() {
        return locked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean canLogin() {
        return !locked;
    }

    public void assertUserNotLocked() {
        if (this.isLocked()) {
            throw new BusinessException(DomainAuthError.USER_LOCKED);
        }
    }
    
}
