package com.example.auth_service.application.DTO.repone;

public record UserOverviewResponse(long totalAccounts, long totalShops, long inactiveShops,
                                   long lockedAccounts, long activeUsers) {
}
