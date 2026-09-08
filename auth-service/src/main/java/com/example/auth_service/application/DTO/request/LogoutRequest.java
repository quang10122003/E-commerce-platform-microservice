package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank(message = "refreshToken is required")
        String refreshToken ,
        @NotBlank(message = "accessToken is required")
        String accessToken) {
}
