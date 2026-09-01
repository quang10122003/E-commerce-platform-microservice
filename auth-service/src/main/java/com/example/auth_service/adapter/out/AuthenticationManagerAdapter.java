package com.example.auth_service.adapter.out;

import com.example.auth_service.application.DTO.repone.AuthenticatedUser;
import com.example.auth_service.application.port.out.AuthenticationManagerPort;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.auth_service.adapter.entity.UserEntity;
import com.example.auth_service.adapter.mapper.UserMapper;


@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationManagerAdapter implements AuthenticationManagerPort {
    AuthenticationManager authenticationManager;
    UserMapper userMapper;
    @Override
    public AuthenticatedUser authenticate(String email, String password) {
        // Xác thực email và mật khẩu qua Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        // Lấy user đã đăng nhập thành công từ principal
        UserEntity userEntity = (UserEntity) authentication.getPrincipal();


        return AuthenticatedUser.builder().user(userMapper.toDomain(userEntity))
                .build();
    }
}
