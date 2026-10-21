package com.example.common.web;

import com.example.common.error.ApiErrorDto;
import com.example.common.error.ErrorCode;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        AuthorizationError error = AuthorizationError.ACCESS_DENIED;
        log.warn("Request bị từ chối do không đủ quyền: error_code={}", error.getCode());
        return ResponseEntity.status(error.getHttpStatusCode())
                .body(ApiResponse.error(error.getMessage(), new ApiErrorDto(error.getCode(), error.getMessage())));
    }

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
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HandlerMethodValidationException.class,
            MissingServletRequestPartException.class,
            HttpMessageNotReadableException.class,
            MultipartException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception ex) {
        log.warn("Request không hợp lệ: type={}, message={}",
                ex.getClass().getSimpleName(), ex.getMessage());
        return badRequest("BAD_REQUEST", "Request không hợp lệ: " + ex.getMessage());
    }

    // Trả 405 và header Allow khi client gọi sai HTTP method, thay vì báo lỗi hệ thống.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String message = "HTTP method không được hỗ trợ cho endpoint này";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(ex.getHeaders())
                .body(ApiResponse.error(message, new ApiErrorDto("METHOD_NOT_ALLOWED", message)));
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
        log.error("Request thất bại do lỗi hệ thống", ex);
        ApiErrorDto errorDto = new ApiErrorDto("INTERNAL_ERROR", "Unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred; please check the server console.", errorDto));
    }
}
