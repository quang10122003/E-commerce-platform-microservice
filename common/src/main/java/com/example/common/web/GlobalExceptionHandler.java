package com.example.common.web;

import com.example.common.error.ApiErrorDto;
import com.example.common.error.ErrorCode;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import lombok.extern.slf4j.Slf4j;
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
        // Dùng message từ exception để giữ lại chi tiết động như các chỉ số đầu vào.
        String message = ex.getMessage();
        ApiErrorDto detailedError = new ApiErrorDto(errorCode.getCode(), message);
        return ResponseEntity.status(errorCode.getHttpStatusCode())
                .body(ApiResponse.error(message, detailedError));
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

    // Ghi log đầy đủ lỗi chưa được phân loại nhưng không trả stack trace cho client.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Lỗi không mong muốn khi xử lý request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Unexpected error occurred",
                        new ApiErrorDto("INTERNAL_ERROR", "Unexpected error occurred")));
    }

    // Tạo response 400 thống nhất cho lỗi request từ phía client.
    private ResponseEntity<ApiResponse<Void>> badRequest(String code, String message) {
        ApiErrorDto errorDto = new ApiErrorDto(code, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(message, errorDto));
    }

}
