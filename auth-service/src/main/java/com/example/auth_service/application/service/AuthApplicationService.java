package com.example.auth_service.application.service;

import java.time.Duration;
import java.time.Clock;
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
import com.example.auth_service.application.DTO.request.ChangePasswordRequest;
import com.example.auth_service.application.DTO.command.OAuthLoginCommand;
import com.example.auth_service.application.DTO.request.ExchangeOAuthLoginCodeRequest;
import com.example.auth_service.application.port.in.ChangePasswordUseCase;
import com.example.auth_service.application.constant.AuthCacheKeys;
import com.example.auth_service.application.constant.Constant;
import com.example.auth_service.application.port.out.*;
import com.example.auth_service.application.port.out.cache.CachePort;
import com.example.auth_service.domain.model.AuthProvider;
import com.example.auth_service.domain.model.OutboxEvent;
import com.example.auth_service.domain.model.Role;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.domain.model.RoleEnum;
import com.example.common.exception.BusinessException;
import com.example.common.exception.CacheAccessException;
import com.example.common.security.AccessControlCacheKeys;
import com.example.common.untill.ValidationUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import org.springframework.transaction.annotation.Transactional;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService implements LoginUseCase, RegisterUseCase , RefreshTokenUseCase, ChecktokenUseCase, LogoutUserCase,
        ChangePasswordUseCase, ExchangeOAuthLoginCodeUseCase {
    AuthenticationManagerPort authenticationManagerPort;

    TokenServicePort tokenServicePort;

    RoleRepositoryPort roleRepositoryPort;
    UserRepositoryPort userRepositoryPort;
    OutboxEventPort outboxEventPort;
    CachePort cachePort;
    Clock clock;

    @Transactional(readOnly = true)
    @Override
    public AuthResponse login(LoginRequest request) {

        String normalizedEmail = ValidationUtils.normalizeEmail(request.getEmail());

        AuthenticatedUser authenticatedUser = authenticationManagerPort.authenticate(
                normalizedEmail,
                request.getPassword());

        User user = authenticatedUser.getUser();
        user.assertUserNotLocked();

        log.info("Đăng nhập thành công: user_id={}", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional
    @Override
    public AuthResponse register(RegisterRquest request) {
        User user = createNewUser(request);
        publishUserRegisteredEvent(user);
        log.info("Đăng ký tài khoản thành công: user_id={}", user.getId());
        return buildAuthResponse(user);
    }

    // Ưu tiên tìm theo sub Google; chỉ gộp theo email khi Google xác nhận email để tránh chiếm tài khoản.
    @Transactional
    public User loginWithGoogle(OAuthLoginCommand command) {
        if (!command.emailVerified()) {
            throw new BusinessException(AuthError.OAUTH_EMAIL_NOT_VERIFIED);
        }
        Optional<User> linkedUser = userRepositoryPort.findByProviderId(command.providerId());
        if (linkedUser.isPresent()) {
            linkedUser.get().assertUserNotLocked();
            return linkedUser.get();
        }
        String email = ValidationUtils.normalizeEmail(command.email());
        Optional<User> existingUser = userRepositoryPort.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.assertUserNotLocked();
            user.linkGoogle(command.providerId());
            log.info("Liên kết Google vào tài khoản sẵn có: user_id={}", user.getId());
            return userRepositoryPort.save(user);
        }
        Role role = roleRepositoryPort.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new BusinessException(AuthError.ROLE_NOT_FOUND));
        User user = userRepositoryPort.save(User.createFromOAuth(email, toFullName(command.fullName(), email),
                AuthProvider.GOOGLE, command.providerId(), Set.of(role)));
        publishUserRegisteredEvent(user);
        log.info("Tạo tài khoản bằng Google thành công: user_id={}", user.getId());
        return user;
    }

    // gen code đổi token khi đnagw nhap gg
    public String issueOAuthLoginCode(User user) {
        String code = UUID.randomUUID().toString();
        cachePort.set(AuthCacheKeys.oauthLoginCode(code), user.getId(), Constant.OAUTH_LOGIN_CODE_TTL);
        return code;
    }

//    đổi code lấy token
    @Transactional(readOnly = true)
    @Override
    public AuthResponse exchange(ExchangeOAuthLoginCodeRequest request) {
        Long userId = cachePort.getAndDelete(AuthCacheKeys.oauthLoginCode(request.code()), Long.class)
                .orElseThrow(() -> new BusinessException(AuthError.OAUTH_LOGIN_CODE_INVALID));
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.assertUserNotLocked();
        log.info("Đổi mã đăng nhập OAuth thành token: user_id={}", userId);
        return buildAuthResponse(user);
    }


    @Override
    public RefreshTokenRepone refreshToken(RefreshTokenRequest request) {
        String refreshTokenRequest = request.refreshToken();
        String email = tokenServicePort.getEmailFromToken(refreshTokenRequest);
        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.assertUserNotLocked();
        if(!tokenServicePort.isRefreshTokenValid(refreshTokenRequest,user)){
           throw  new BusinessException(AuthError.REFRESH_TOKEN_INVALID);
        }
        // SETNX vừa kiểm tra vừa đánh dấu đã dùng: token đã đăng xuất hoặc hai request làm mới đồng thời đều bị từ chối.
        if (!cachePort.setIfAbsent(AccessControlCacheKeys.blacklistJwt(refreshTokenRequest), "1",
                tokenServicePort.getRefreshTokenTtl())) {
            throw new BusinessException(AuthError.REFRESH_TOKEN_INVALID);
        }

        String accessToken = tokenServicePort.generateAccessToken(user);
        String refreshToken = tokenServicePort.generateRefreshToken(user);

        // Không ghi token vào log vì đây là thông tin xác thực.
        log.info("Làm mới token thành công: user_id={}", user.getId());

        return RefreshTokenRepone.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    @Override
    public AccessTokenValidationResponse CheckToken(String authorizationHeader) {
        String token  = tokenServicePort.extractBearerToken(authorizationHeader);

        String email = tokenServicePort.getEmailFromToken(token);

        User user = userRepositoryPort.findByEmail(email).orElseThrow(()-> new BusinessException(AuthError.USER_NOT_FOUND));
        user.assertUserNotLocked();
        boolean isValid = tokenServicePort.isAccessTokenValid(token,user);

        // Không ghi token vào log vì đây là thông tin xác thực.
        log.debug("Kiểm tra access token: user_id={}, hợp lệ={}", user.getId(), isValid);

        return AccessTokenValidationResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();

    }

    @Override
    public void logout(LogoutRequest logoutRequest) {
        String accessToken = logoutRequest.accessToken();
        String refreshToken = logoutRequest.refreshToken();

        revokeAccessToken(logoutRequest.accessToken());
        revokeRefreshToken(logoutRequest.refreshToken());

    }


    // Xác minh mật khẩu hiện tại rồi lưu mật khẩu mới đã mã hóa; không ghi mật khẩu vào log.
    // shortcut: chưa thu hồi phiên đăng nhập khác (cần passwordChangedAt + gateway kiểm tra iat), làm khi có yêu cầu đăng xuất mọi thiết bị.
    @Override
    @Transactional
    public void changePassword(String authorizationHeader, ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(AuthError.PASSWORD_CONFIRM_MISMATCH);
        }
        Long userId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.assertUserNotLocked();
        user.assertHasPassword();
        if (!authenticationManagerPort.matchesPassword(request.currentPassword(), user.getPassword())) {
            log.warn("Đổi mật khẩu thất bại do mật khẩu hiện tại không đúng: user_id={}", userId);
            throw new BusinessException(AuthError.CURRENT_PASSWORD_INCORRECT);
        }
        if (authenticationManagerPort.matchesPassword(request.newPassword(), user.getPassword())) {
            throw new BusinessException(AuthError.NEW_PASSWORD_SAME_AS_CURRENT);
        }
        user.changePassword(authenticationManagerPort.encodePassword(request.newPassword()));
        userRepositoryPort.save(user);
        log.info("Đổi mật khẩu thành công: user_id={}", userId);
    }

    private void publishUserRegisteredEvent(User user) {
        UUID eventId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now(clock);
        outboxEventPort.save(new OutboxEvent(
                eventId, Constant.USER, String.valueOf(user.getId()), UserRegisteredEventDto.Event_Type,
                new UserRegisteredEventDto(eventId, UserRegisteredEventDto.Event_Type,
                        user.getId(), user.getEmail(), user.getFullName(), now), now));
    }

    private User createNewUser(RegisterRquest request) {
        String email = ValidationUtils.normalizeEmail(request.getEmail());
        if (userRepositoryPort.findByEmail(email).isPresent()) {
            throw new BusinessException(AuthError.EMAIL_ALREADY_REGISTERED);
        }
        Role role = roleRepositoryPort.findByName(RoleEnum.ROLE_USER)
                .orElseThrow(() -> new BusinessException(AuthError.ROLE_NOT_FOUND));
        String hashedPassword = authenticationManagerPort.encodePassword(request.getPassword());
        String fullName = ValidationUtils.normalize(request.getFullName());
        User newUser = User.create(email, hashedPassword, fullName, Set.of(role));
        return userRepositoryPort.save(newUser);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .userId(user.getId())
                .accessToken(tokenServicePort.generateAccessToken(user))
                .refreshToken(tokenServicePort.generateRefreshToken(user))
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    // Tên Google có thể rỗng hoặc dài hơn giới hạn họ tên của hệ thống.
    private String toFullName(String fullName, String email) {
        String name = fullName == null || fullName.isBlank() ? email.substring(0, email.indexOf('@')) : fullName.trim();
        return name.length() > User.MAX_FULL_NAME_LENGTH ? name.substring(0, User.MAX_FULL_NAME_LENGTH) : name;
    }

    private void revokeAccessToken(String accessToken) {
        blacklistToken(accessToken, tokenServicePort.getAccessTokenTtl());
    }
    private void revokeRefreshToken(String refreshToken) {
        blacklistToken(refreshToken, tokenServicePort.getRefreshTokenTtl());
    }

    private void blacklistToken(String token, Duration ttl) {
        try {
            cachePort.set(AccessControlCacheKeys.blacklistJwt(token), "1", ttl);
        } catch (CacheAccessException exception) {
            log.error("Không thể ghi trạng thái thu hồi token vào cache");
        }
    }


}
