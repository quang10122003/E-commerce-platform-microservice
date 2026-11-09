package com.example.notification.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateBannerStatusRequest(@NotNull Boolean active) {
}
