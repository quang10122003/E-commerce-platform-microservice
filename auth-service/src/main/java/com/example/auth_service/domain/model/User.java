package com.example.auth_service.domain.model;

import java.time.Instant;
import java.util.Set;

import com.example.auth_service.domain.error.DomainAuthError;
import com.example.common.exception.BusinessException;

public class User {

    public static final int MAX_FULL_NAME_LENGTH = 30;
    public static final int MAX_SHOP_NAME_LENGTH = 100;
    public static final int MAX_SHOP_DESCRIPTION_LENGTH = 2000;

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
    private String avatarUrl;
    private String avatarObjectPath;
    private AuthProvider provider;
    private String providerId;

    public User(Long id, String email, String password, String fullName, String shopName,
                String shopLogoUrl, String shopDescription, String shopPhone, String shopAddress,
                Instant shopCreatedAt, boolean shopLock, Set<Role> roles, boolean locked,
                Instant createdAt, Instant updatedAt, Instant deletedAt, String shopLogoObjectPath,
                String avatarUrl, String avatarObjectPath, AuthProvider provider, String providerId) {
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
        this.avatarUrl = avatarUrl;
        this.avatarObjectPath = avatarObjectPath;
        this.provider = provider == null ? AuthProvider.LOCAL : provider;
        this.providerId = providerId;
    }

    // ID và thời gian do persistence sinh, không tự gán khi đăng ký.
    public static User create(
            String email,
            String password,
            String fullName,
            Set<Role> roles) {
        return new User(null, email, password, fullName, null, null, null, null, null,
                null, false, roles, false, null, null, null, null, null, null, AuthProvider.LOCAL, null);
    }

    // Tài khoản tạo qua OAuth không có mật khẩu, chỉ đăng nhập được bằng provider.
    public static User createFromOAuth(
            String email,
            String fullName,
            AuthProvider provider,
            String providerId,
            Set<Role> roles) {
        return new User(null, email, null, fullName, null, null, null, null, null,
                null, false, roles, false, null, null, null, null, null, null, provider, providerId);
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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getAvatarObjectPath() {
        return avatarObjectPath;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public boolean hasPassword() {
        return provider != AuthProvider.GOOGLE;
    }

    // Gắn Google vào tài khoản mật khẩu cùng email đã xác minh, giữ nguyên toàn bộ dữ liệu cũ.
    public void linkGoogle(String providerId) {
        assertNotDeleted();
        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("providerId không được rỗng");
        }
        this.providerId = providerId;
        if (provider == AuthProvider.LOCAL) {
            provider = AuthProvider.LOCAL_GOOGLE;
        }
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

    // Đổi tên và mô tả shop (bỏ khoảng trắng đầu cuối, mô tả rỗng lưu null); chỉ shop còn hoạt động mới được sửa.
    public void changeShopProfile(String shopName, String shopDescription) {
        assertCanEditShop();
        String name = shopName == null ? "" : shopName.trim();
        if (name.isEmpty() || name.length() > MAX_SHOP_NAME_LENGTH) {
            throw new BusinessException(DomainAuthError.INVALID_SHOP_NAME);
        }
        String description = shopDescription == null ? "" : shopDescription.trim();
        if (description.length() > MAX_SHOP_DESCRIPTION_LENGTH) {
            throw new BusinessException(DomainAuthError.INVALID_SHOP_DESCRIPTION);
        }
        this.shopName = name;
        this.shopDescription = description.isEmpty() ? null : description;
    }

    public void changeShopLogo(String shopLogoUrl, String shopLogoObjectPath) {
        assertCanEditShop();
        this.shopLogoUrl = shopLogoUrl;
        this.shopLogoObjectPath = shopLogoObjectPath;
    }

    // Thay mật khẩu bằng chuỗi đã mã hóa; tài khoản đã xóa không còn mật khẩu nên không được đổi.
    public void changePassword(String encodedPassword) {
        assertNotDeleted();
        assertHasPassword();
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu đã mã hóa không được rỗng");
        }
        this.password = encodedPassword;
    }

    // Đổi họ tên hiển thị (bỏ khoảng trắng đầu cuối); tài khoản đã xóa hoặc tên rỗng, quá 30 ký tự thì từ chối.
    public void changeFullName(String fullName) {
        assertNotDeleted();
        String trimmed = fullName == null ? "" : fullName.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_FULL_NAME_LENGTH) {
            throw new BusinessException(DomainAuthError.INVALID_FULL_NAME);
        }
        this.fullName = trimmed;
    }

    // Đổi ảnh đại diện sang file mới đã upload; tài khoản đã xóa thì không được đổi.
    public void changeAvatar(String avatarUrl, String avatarObjectPath) {
        assertNotDeleted();
        this.avatarUrl = avatarUrl;
        this.avatarObjectPath = avatarObjectPath;
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
        avatarUrl = null;
        avatarObjectPath = null;
        // Bỏ liên kết để tài khoản Google này có thể tạo tài khoản mới sau khi xóa.
        providerId = null;
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

    public void assertHasPassword() {
        if (!hasPassword()) {
            throw new BusinessException(DomainAuthError.PASSWORD_NOT_SUPPORTED);
        }
    }

    public void assertUserNotLocked() {
        if (this.isLocked()) {
            throw new BusinessException(DomainAuthError.USER_LOCKED);
        }
    }
    
}
