package com.example.auth_service.adapter.out.persistence.projection;

import java.time.Instant;

public interface UserListProjection {
    Long getId();
    String getEmail();
    String getFullName();
    boolean getLocked();
    Instant getCreatedAt();
    String getShopName();
    String getShopLogoUrl();
    String getShopDescription();
    String getShopPhone();
    String getShopAddress();
    Instant getShopCreatedAt();
    boolean getShopLocked();
    Instant getDeletedAt();
}
