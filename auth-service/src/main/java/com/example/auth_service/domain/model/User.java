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
    private Instant deletedAt;
    private String shopLogoObjectPath;

    public User(Long id, String email, String password, String fullName, String shopName,
                String shopLogoUrl, String shopDescription, String shopPhone, String shopAddress,
                Instant shopCreatedAt, boolean shopLock, Set<Role> roles, boolean locked,
                Instant createdAt, Instant updatedAt, Instant deletedAt, String shopLogoObjectPath) {
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
        this.deletedAt = deletedAt;
        this.shopLogoObjectPath = shopLogoObjectPath;
    }

    // ID và thời gian do persistence sinh, không tự gán khi đăng ký.
    public static User create(
            String email,
            String password,
            String fullName,
            Set<Role> roles) {
        return new User(null, email, password, fullName, null, null, null, null, null,
                null, false, roles, false, null, null, null, null);
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

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getShopLogoObjectPath() {
        return shopLogoObjectPath;
    }

    // Chỉ shop còn hoạt động mới được sửa hồ sơ shop.
    public void assertCanEditShop() {
        assertNotDeleted();
        if (roles.stream().noneMatch(role -> role.getName() == RoleEnum.ROLE_SHOP)) {
            throw new BusinessException(DomainAuthError.SHOP_NOT_FOUND);
        }
        if (shopLock) {
            throw new BusinessException(DomainAuthError.SHOP_LOCKED);
        }
    }

    public void changeShopLogo(String shopLogoUrl, String shopLogoObjectPath) {
        assertCanEditShop();
        this.shopLogoUrl = shopLogoUrl;
        this.shopLogoObjectPath = shopLogoObjectPath;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean canLogin() {
        return !locked;
    }

    public void toggleUserLock() {
        assertNotDeleted();
        if (!locked && roles.stream().anyMatch(role -> role.getName() == RoleEnum.ROLE_ADMIN)) {
            throw new BusinessException(DomainAuthError.ADMIN_LOCK_FORBIDDEN);
        }
        locked = !locked;
    }

    public void toggleShopLock() {
        assertNotDeleted();
        if (roles.stream().noneMatch(role -> role.getName() == RoleEnum.ROLE_SHOP)) {
            throw new BusinessException(DomainAuthError.SHOP_NOT_FOUND);
        }
        shopLock = !shopLock;
    }

    // Xóa thông tin cá nhân nhưng giữ ID để dữ liệu ở service khác vẫn tham chiếu được.
    public void anonymize(Instant now) {
        assertNotDeleted();
        if (roles.stream().anyMatch(role -> role.getName() == RoleEnum.ROLE_ADMIN)) {
            throw new BusinessException(DomainAuthError.ADMIN_DELETE_FORBIDDEN);
        }
        boolean shop = roles.stream().anyMatch(role -> role.getName() == RoleEnum.ROLE_SHOP);
        email = "deleted-" + id + "@deleted.local";
        password = null;
        fullName = "Người dùng đã xóa";
        shopName = shop ? "Shop đã ngừng hoạt động" : null;
        shopLogoUrl = null;
        shopLogoObjectPath = null;
        shopDescription = null;
        shopPhone = null;
        shopAddress = null;
        locked = true;
        shopLock = shop || shopLock;
        deletedAt = now;
    }

    private void assertNotDeleted() {
        if (isDeleted()) {
            throw new BusinessException(DomainAuthError.USER_DELETED);
        }
    }

    public void assertUserNotLocked() {
        if (this.isLocked()) {
            throw new BusinessException(DomainAuthError.USER_LOCKED);
        }
    }
    
}
