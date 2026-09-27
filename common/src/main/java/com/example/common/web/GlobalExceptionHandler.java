package com.example.common.web;

import com.example.common.error.ApiErrorDto;
import com.example.common.error.ErrorCode;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

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

    // Ghi nhận lỗi binding/validation xảy ra trước khi controller được gọi.
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingServletRequestPartException.class,
            HttpMessageNotReadableException.class,
            MultipartException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        log.warn("Request không hợp lệ: type={}, message={}",
                ex.getClass().getSimpleName(), ex.getMessage());
        return badRequest("BAD_REQUEST", "Request không hợp lệ: " + ex.getMessage());
    }

    // Tạo response 400 thống nhất cho lỗi request từ phía client.
    private ResponseEntity<ApiResponse<Void>> badRequest(String code, String message) {
        ApiErrorDto errorDto = new ApiErrorDto(code, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, errorDto));
    }

    // Trả lỗi chung để không lộ chi tiết stack trace ra response cho các lỗi 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        ApiErrorDto errorDto = new ApiErrorDto("INTERNAL_ERROR", "Unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred; please check the server console.", errorDto));
    }
}