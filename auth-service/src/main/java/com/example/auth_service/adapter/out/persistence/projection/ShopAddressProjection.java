package com.example.auth_service.adapter.out.persistence.projection;

import java.time.Instant;

public interface ShopAddressProjection {
    Long getShopId();
    String getShopName();
    String getShopAddress();
    String getShopLogoUrl();
    String getShopDescription();
    Instant getShopCreatedAt();
}

