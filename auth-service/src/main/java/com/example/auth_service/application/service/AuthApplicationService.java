package com.example.auth_service.application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.auth_service.application.DTO.repone.AccessTokenValidationResponse;
import com.example.auth_service.application.DTO.repone.RefreshTokenRepone;
import com.example.auth_service.application.DTO.request.LogoutRequest;
import com.example.auth_service.application.DTO.request.RefreshTokenRequest;
import com.example.auth_service.application.port.in.*;

import com.example.auth_service.application.DTO.repone.AuthResponse;
import com.example.auth_service.application.DTO.repone.AuthenticatedUser;
import com.example.auth_service.application.DTO.event.UserRegisteredEventDto;
import com.example.auth_service.application.DTO.request.LoginRequest;
import com.example.auth_service.application.DTO.request.RegisterRquest;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.port.out.*;
import com.example.auth_service.domain.model.OutboxEvent;
import com.example.auth_service.domain.model.Role;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.domain.until.EventType;
import com.example.auth_service.domain.until.RoleEnum;
import com.example.common.exception.BusinessException;
import com.example.common.untill.ValidationUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService implements LoginUseCase, RegisterUseCase , RefreshTokenUseCase, ChecktokenUseCase, LogoutUserCase {
    AuthenticationManagerPort authenticationManagerPort;

    TokenServicePort tokenServicePort;

    RoleRepositoryPort roleRepositoryPort;
    UserRepositoryPort userRepositoryPort;
    OutboxEventPort outboxEventPort;
    AccessControlCachePort accessControlCachePort;

    @Override
    public AuthResponse login(LoginRequest request) {

        String normalizedEmail = ValidationUtils.normalizeEmail(request.getEmail());

        // Xác thực qua port để không phụ thuộc trực tiếp vào Spring Security
        AuthenticatedUser authenticatedUser = authenticationManagerPort.authenticate(
                normalizedEmail,
                request.getPassword());

        User user = authenticatedUser.getUser();
        assertUserNotLocked(user);

        String accectToken = tokenServicePort.generateAccessToken(user);

        String refreshToken = tokenServicePort.generateRefreshToken(user);
        log.info("Đăng nhập thành công: user_id={}", user.getId());
        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(accectToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRoles().stream().map(Role::getName).collect(
                        Collectors.toSet()))
                .build();
    }


    @Override
    public AuthResponse register(RegisterRquest request) {
        String normalizedEmail = ValidationUtils.normalizeEmail(request.getEmail());
        String password = request.getPassword();
        String fullName = ValidationUtils.normalize(request.getFullName());

        Role role = roleRepositoryPort.findByName(RoleEnum.User.getName())
                .orElseThrow(() -> new BusinessException(AuthError.ROLE_NOT_FOUND));

        Optional<User> userOptional = userRepositoryPort.findByEmail(normalizedEmail);
        User usersave;
        // tk đã tồn tại
        if (userOptional.isPresent()) {
            throw new BusinessException(AuthError.EMAIL_ALREADY_REGISTERED);
        } else {
            // tk chưa tồn tại tạo tk
            String hashPassword  = authenticationManagerPort.encodePassword(password);
            User newUser = User.create(normalizedEmail, hashPassword, fullName, Set.of(role));
            usersave = userRepositoryPort.save(newUser);
        }
        // tạo id cho event outBox
        UUID eventId = UUID.randomUUID();
        EventType eventType = EventType.USER_REGISTERED;
        outboxEventPort.save(new OutboxEvent(
                eventId,
                "User",
                String.valueOf(usersave.getId()),
                eventType,
                new UserRegisteredEventDto(
                        eventId,
                        eventType.getValue(),
                        usersave.getId(),
                        usersave.getEmail(),
                        usersave.getFullName(),
                        LocalDateTime.now())));
                        
        String accessToken = tokenServicePort.generateAccessToken(usersave);
        String refreshToken = tokenServicePort.generateRefreshToken(usersave);

        log.info("Đăng ký tài khoản thành công: user_id={}", usersave.getId());

        return AuthResponse.builder()
                .userId(usersave.getId())
                .accessToken(
                        accessToken)
                .refreshToken(refreshToken)
                .email(usersave.getEmail())
                .fullName(usersave.getFullName())
                .role(usersave.getRoles().stream().map(Role::getName).collect(
                        Collectors.toSet()))
                .build();

    }


    @Override
    public RefreshTokenRepone refreshToken(RefreshTokenRequest request) {
        String refreshTokenRequest = request.refreshToken();
        String email = tokenServicePort.getEmailFromToken(refreshTokenRequest);
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        // check tài khoản bị khóa hay k
        assertUserNotLocked(user);
        // check token hợp lệ vs user hay k
        if(!tokenServicePort.isRefreshTokenValid(refreshTokenRequest,user)){
           throw  new BusinessException(AuthError.REFRESH_TOKEN_INVALID);
        }

        String accessToken = tokenServicePort.generateAccessToken(user);
        String refreshToken = tokenServicePort.generateRefreshToken(user);

        // Ghi nhận kết quả cấp lại token, không ghi access token hoặc refresh token vào log.
        log.info("Làm mới token thành công: user_id={}", user.getId());

        return RefreshTokenRepone.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Override
    public AccessTokenValidationResponse CheckToken(String authorizationHeader) {
        String token  = tokenServicePort.extractBearerToken(authorizationHeader);

        String email = tokenServicePort.getEmailFromToken(token);

        User user = userRepositoryPort.findByEmail(email).orElseThrow(()-> new BusinessException(AuthError.USER_NOT_FOUND));
        assertUserNotLocked(user);
        boolean isValid = tokenServicePort.isAccessTokenValid(token,user);

        // Ghi nhận kết quả kiểm tra token để hỗ trợ truy vết request mà không lộ token.
        log.debug("Kiểm tra access token: user_id={}, hợp lệ={}", user.getId(), isValid);

        return AccessTokenValidationResponse.builder().validateToken(isValid).build();

    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        String accessToken = logoutRequest.accessToken();
        String refreshToken = logoutRequest.refreshToken();

        Duration ttlAccessToken = tokenServicePort.getAccessTokenTtl();
        Duration ttlRefreshToken = tokenServicePort.getRefreshTokenTtl();

        accessControlCachePort.blacklistToken(accessToken,ttlAccessToken);
        accessControlCachePort.blacklistToken(refreshToken,ttlRefreshToken);

    }

    // Kiểm tra tài khoản có bị khóa hay không trước khi cấp token.
    private void assertUserNotLocked(User user) {
        if (user.isLocked()) {
            throw new BusinessException(AuthError.USER_LOCKED);
        }
    }



}
