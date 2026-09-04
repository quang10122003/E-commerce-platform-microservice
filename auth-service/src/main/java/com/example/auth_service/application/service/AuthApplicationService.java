package com.example.auth_service.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.example.auth_service.application.DTO.repone.AuthResponse;
import com.example.auth_service.application.DTO.repone.AuthenticatedUser;
import com.example.auth_service.application.DTO.event.UserRegisteredEventDto;
import com.example.auth_service.application.DTO.request.LoginRequest;
import com.example.auth_service.application.DTO.request.RegisterRquest;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.port.in.LoginUseCase;
import com.example.auth_service.application.port.in.RegisterUseCase;
import com.example.auth_service.application.port.out.AuthenticationManagerPort;
import com.example.auth_service.application.port.out.OutboxEventPort;
import com.example.auth_service.application.port.out.RoleRepositoryPort;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.OutboxEvent;
import com.example.auth_service.domain.model.Role;
import com.example.auth_service.domain.model.User;
import com.example.auth_service.domain.until.EventType;
import com.example.auth_service.domain.until.RoleEnum;
import com.example.common.exception.ApplicationException;
import com.example.common.untill.ValidationUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService implements LoginUseCase, RegisterUseCase {
    AuthenticationManagerPort authenticationManagerPort;

    TokenServicePort tokenServicePort;

    ValidationUtils validationUtils;
    RoleRepositoryPort roleRepositoryPort;
    UserRepositoryPort userRepositoryPort;
    OutboxEventPort outboxEventPort;

    @Override
    public AuthResponse login(LoginRequest request) {

        String normalizedEmail = validationUtils.normalizeEmail(request.getEmail());

        // Xác thực qua port để không phụ thuộc trực tiếp vào Spring Security
        AuthenticatedUser authenticatedUser = authenticationManagerPort.authenticate(
                normalizedEmail,
                request.getPassword());

        User user = authenticatedUser.getUser();

        String accectToken = tokenServicePort.generateAccessToken(user);

        String refreshToken = tokenServicePort.generateRefreshToken(user);
        log.info("Đăng nhập thành công, userId={}, email={}", user.getId(), user.getEmail());
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
    @Transactional
    public AuthResponse register(RegisterRquest request) {
        String normalizedEmail = validationUtils.normalizeEmail(request.getEmail());
        String password = request.getPassword();
        String fullName = validationUtils.normalize(request.getFullName());

        Role role = roleRepositoryPort.findByName(RoleEnum.User.getName())
                .orElseThrow(() -> new ApplicationException(AuthError.ROLE_NOT_FOUND));

        Optional<User> userOptional = userRepositoryPort.findByEmail(normalizedEmail);
        User usersave;
        // tk đã tồn tại
        if (userOptional.isPresent()) {
            throw new ApplicationException(AuthError.EMAIL_ALREADY_REGISTERED);
        } else {
            // tk chưa tồn tại tạo tk
            User newUser = User.create(normalizedEmail, password, fullName, Set.of(role));
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

        log.info(
                "Đăng ký thành công, userId={}, email={}",
                usersave.getId(),
                usersave.getEmail());

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
}
