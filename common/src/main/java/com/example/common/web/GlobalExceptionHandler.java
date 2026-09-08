package com.example.common.web;

import com.example.common.error.ApiErrorDto;
import com.example.common.error.ErrorCode;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        // Ghi mã lỗi nghiệp vụ để tra soát theo request_id trong MDC, không ghi stack trace lỗi dự kiến.
        log.warn("Request thất bại do lỗi nghiệp vụ: error_code={}, http_status={}",
                errorCode.getCode(), errorCode.getHttpStatusCode());
        ApiErrorDto errorDto = new ApiErrorDto(errorCode.getCode(), errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(ApiResponse.error(errorCode.getMessage(), errorDto));
    }

//    // Trả lỗi chung để không lộ chi tiết stack trace ra response.
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
//        ApiErrorDto errorDto = new ApiErrorDto("INTERNAL_ERROR", "Unexpected error occurred");
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error("Unexpected error occurred", errorDto));
//    }
}
