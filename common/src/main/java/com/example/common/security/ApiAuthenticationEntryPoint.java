package com.example.common.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.example.common.error.ApiErrorDto;
import com.example.common.error.AuthorizationError;
import com.example.common.response.ApiResponse;
import com.example.common.untill.JsonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Chuyển response lỗi xác thực sang JSON theo định dạng API chung.
     JsonUtils jsonUtils;


    // Trả lỗi 401 khi request chưa được xác thực.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        AuthorizationError authorizationError = AuthorizationError.UNAUTHENTICATED;
        ApiErrorDto error = new ApiErrorDto(authorizationError.getCode(), authorizationError.getMessage());
        response.setStatus(authorizationError.getHttpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonUtils.toJson(ApiResponse.error(authorizationError.getMessage(), error)));
    }
}
