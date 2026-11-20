package com.example.auth_service.adapter.in.oauth2;

import com.example.auth_service.application.DTO.command.OAuthLoginCommand;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.port.in.OAuthLoginUseCase;
import com.example.common.exception.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    OAuthLoginUseCase oauthLoginUseCase;
    String frontendCallbackUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User principal = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuthLoginCommand command = new OAuthLoginCommand(
                principal.getAttribute("email"),
                Boolean.TRUE.equals(principal.getAttribute("email_verified")),
                principal.getAttribute("sub"),
                principal.getAttribute("name"));
        // Session chỉ dùng để giữ state OAuth; API vẫn xác thực bằng JWT nên hủy ngay sau đăng nhập.
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        UriComponentsBuilder redirect = UriComponentsBuilder.fromUriString(frontendCallbackUrl);
        try {
            // gen code đổi tokenn
            redirect.queryParam("code", oauthLoginUseCase.login(registrationId, command));
        } catch (BusinessException exception) {
            log.warn("Đăng nhập OAuth bị từ chối: provider={}, code={}", registrationId, exception.getErrorCode().getCode());
            redirect.queryParam("error", exception.getErrorCode().getCode());
        } catch (RuntimeException exception) {
            // Trình duyệt đang ở bước redirect nên lỗi hạ tầng cũng phải đưa người dùng về FE thay vì trang lỗi trắng.
            log.error("Đăng nhập OAuth thất bại: provider={}", registrationId, exception);
            redirect.queryParam("error", AuthError.OAUTH_LOGIN_FAILED.getCode());
        }
        // Redirect FE kèm code đổi token
        response.sendRedirect(redirect.build().encode().toUriString());
    }
}
