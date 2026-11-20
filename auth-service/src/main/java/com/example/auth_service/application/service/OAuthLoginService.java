package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.command.OAuthLoginCommand;
import com.example.auth_service.application.port.in.OAuthLoginUseCase;
import com.example.auth_service.application.registry.OAuthLoginHandlerRegistry;
import com.example.auth_service.domain.model.User;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class OAuthLoginService implements OAuthLoginUseCase {
    OAuthLoginHandlerRegistry oauthLoginHandlerRegistry;
    AuthApplicationService authApplicationService;



    // tạo tài khoản trong db,= khi đăng nhâp bằng gg và gen code đổi token
    @Override
    public String login(String registrationId, OAuthLoginCommand command) {
        User user = oauthLoginHandlerRegistry.handle(registrationId).login(command);
        log.info("Đăng nhập OAuth thành công: provider={}, user_id={}", registrationId, user.getId());
        return authApplicationService.issueOAuthLoginCode(user);
    }
}
