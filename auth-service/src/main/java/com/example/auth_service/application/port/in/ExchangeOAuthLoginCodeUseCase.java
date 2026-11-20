package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.repone.AuthResponse;
import com.example.auth_service.application.DTO.request.ExchangeOAuthLoginCodeRequest;

public interface ExchangeOAuthLoginCodeUseCase {
    AuthResponse exchange(ExchangeOAuthLoginCodeRequest request);
}
