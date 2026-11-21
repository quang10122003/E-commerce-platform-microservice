package com.example.auth_service.adapter.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.example.auth_service.application.port.out.storage.StorageBucket;
import com.example.auth_service.domain.model.AuthProvider;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(name = "password", columnDefinition = "TEXT")
    String password;

    @Column(name = "full_name", nullable = false)
    String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    @Default
    AuthProvider provider = AuthProvider.LOCAL;

    @Column(length = 255, name = "provider_id")
    String providerId;

    @Column(name = "shop_name")
    String shopName;

    @Column(name = "shop_logo_url", columnDefinition = "TEXT")
    String shopLogoUrl;

    @Column(name = "shop_description", columnDefinition = "TEXT")
    String shopDescription;

    @Column(name = "shop_phone")
    String shopPhone;

    @Column(name = "shop_address", columnDefinition = "TEXT")
    String shopAddress;

    @Column(name = "shop_province_code")
    Integer shopProvinceCode;

    @Column(name = "shop_created_at", columnDefinition = "TIMESTAMP")
    Instant shopCreatedAt;

    @Column(name = "is_shop_lock", nullable = false)
    boolean isShopLock;

    @ManyToMany(fetch = FetchType.EAGER)
    // Chỉ xóa liên kết khi một đầu bị xóa, không cascade REMOVE sang role dùng chung.
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "role_id", nullable = false),
            foreignKey = @ForeignKey(name = "fk_user_roles_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"),
            inverseForeignKey = @ForeignKey(name = "fk_user_roles_role",
                    foreignKeyDefinition = "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE"))
    Set<RoleEntity> roles;

    @Column(name = "is_locked")
    boolean isLocked;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    Instant deletedAt;
    @Column(name = "shop_logo_object_path", length = 500)
    String shopLogoObjectPath;
    @Enumerated(EnumType.STRING)
    @Column(name = "shop_logo_storage_bucket", length = 50)
    StorageBucket shopLogoStorageBucket;
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    String avatarUrl;
    @Column(name = "avatar_object_path", length = 500)
    String avatarObjectPath;
    @Enumerated(EnumType.STRING)
    @Column(name = "avatar_storage_bucket", length = 50)
    StorageBucket avatarStorageBucket;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream().filter(role -> role != null && role.getName() != null)
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isLocked;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

}
