package com.example.auth_service.application.DTO.request;

import jakarta.validation.constraints.NotBlank;

public record ExchangeOAuthLoginCodeRequest(
        @NotBlank(message = "code is required")
        String code
) {
}
