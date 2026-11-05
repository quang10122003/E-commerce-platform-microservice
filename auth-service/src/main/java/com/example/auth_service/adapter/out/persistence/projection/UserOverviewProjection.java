package com.example.auth_service.adapter.out.persistence.projection;

public interface UserOverviewProjection {
    long getTotalAccounts();
    long getTotalShops();
    long getInactiveShops();
    long getLockedAccounts();
    long getActiveUsers();
}
