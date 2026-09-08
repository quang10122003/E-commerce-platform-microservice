package com.example.auth_service.adapter.out.security.authenticationManager;

import com.example.auth_service.application.DTO.repone.AuthenticatedUser;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.port.out.AuthenticationManagerPort;
import com.example.common.exception.BusinessException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.adapter.mapper.UserMapper;


@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationManagerAdapter implements AuthenticationManagerPort {
    AuthenticationManager authenticationManager;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    @Override
    public AuthenticatedUser authenticate(String email, String password) {
        // Xác thực email và mật khẩu qua Spring Security
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException ex) {
            // Chỉ ghi email để hỗ trợ tra soát, tuyệt đối không ghi mật khẩu.
            log.warn("Đăng nhập thất bại do thông tin xác thực không hợp lệ: email={}", email);
            // Chuẩn hóa lỗi Spring Security thành lỗi nghiệp vụ của service.
            throw new BusinessException(AuthError.INVALID_CREDENTIALS, ex);
        }

        // Lấy user đã đăng nhập thành công từ principal
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();


        return AuthenticatedUser.builder().user(userMapper.toDomain(userEntity))
                .build();
    }

    @Override
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
