package com.example.auth_service.adapter.in.oauth2;

import com.example.auth_service.application.error.AuthError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    String frontendCallbackUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        log.warn("Xác thực với Google thất bại: reason={}", exception.getMessage());
        response.sendRedirect(UriComponentsBuilder.fromUriString(frontendCallbackUrl)
                .queryParam("error", AuthError.OAUTH_LOGIN_FAILED.getCode())
                .build().encode().toUriString());
    }
}
