package com.example.notification.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBannerRequest(
        @NotBlank @Size(max = 255) String nameBanner,
        @NotBlank String html) {
}
