package com.example.auth_service.infrastructure.config;

import com.example.auth_service.application.error.AuthError;
import com.example.common.error.ApiErrorDto;
import com.example.common.exception.ApplicationException;
import com.example.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
// (basePackages = "com.example.order")
public class AuthExceptionHandler {

    // handler ApplicationException (doamin exception cx sẽ đc catch thành
    // ApplicationException và trả repone)
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handle(ApplicationException ex) {
        HttpStatus status = mapToHttpStatus(ex.getCodeError());
        ApiErrorDto errorDto = new ApiErrorDto(ex.getCodeError(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage(), errorDto));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handle( BadCredentialsException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
       
        ApiErrorDto errorDto = new ApiErrorDto("INVALID_CREDENTIALS", "Email or password is incorrect");

        return ResponseEntity.status(status)
                .body(ApiResponse.error("Email or password is incorrect", errorDto));
    }

    // map String error code để lấy http status
    private HttpStatus mapToHttpStatus(String code) {
        if (code.equals(AuthError.USER_NOT_FOUND.getCode()))
            return HttpStatus.NOT_FOUND;
        if (code.equals(AuthError.ROLE_NOT_FOUND.getCode()))
            return HttpStatus.NOT_FOUND;
        if (code.equals(AuthError.EMAIL_ALREADY_REGISTERED.getCode()))
            return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}