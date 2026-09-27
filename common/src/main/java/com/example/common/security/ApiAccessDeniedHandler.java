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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    // Chuyển response lỗi phân quyền sang JSON theo định dạng API chung.
    JsonUtils jsonUtils;


    // Trả lỗi 403 khi người dùng đã xác thực nhưng không có quyền truy cập.
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        AuthorizationError authorizationError = AuthorizationError.ACCESS_DENIED;
        ApiErrorDto error = new ApiErrorDto(authorizationError.getCode(), authorizationError.getMessage());
        response.setStatus(authorizationError.getHttpStatusCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonUtils.toJson(ApiResponse.error(authorizationError.getMessage(), error)));
    }
}
