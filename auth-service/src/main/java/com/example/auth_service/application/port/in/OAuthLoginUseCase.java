package com.example.auth_service.application.port.in;

import com.example.auth_service.application.DTO.command.OAuthLoginCommand;

public interface OAuthLoginUseCase {
    // Trả mã dùng một lần thay cho token để token không xuất hiện trên URL redirect.
    String login(String registrationId, OAuthLoginCommand command);
}
