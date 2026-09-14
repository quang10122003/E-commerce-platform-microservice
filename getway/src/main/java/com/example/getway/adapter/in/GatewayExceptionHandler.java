package com.example.getway.adapter.in;

import com.example.common.error.ApiErrorDto;
import com.example.common.exception.BusinessException;
import com.example.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;

import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

// Adapter chuyển BusinessException thành response JSON chuẩn của Gateway.
@Component
@Order(-2)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GatewayExceptionHandler implements WebExceptionHandler {

    ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        if (!(throwable instanceof BusinessException businessException)) {
            // Ghi lại lỗi gateway không thuộc nhóm lỗi nghiệp vụ trước khi chuyển cho handler mặc định.
            log.error("Gateway không xử lý được request: path={}",
                    exchange.getRequest().getPath(), throwable);
            return Mono.error(throwable);
        }

        var errorCode = businessException.getErrorCode();
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatusCode.valueOf(errorCode.getHttpStatusCode()));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> body = ApiResponse.error(
                errorCode.getMessage(),
                new ApiErrorDto(errorCode.getCode(), errorCode.getMessage()));

        try {
            byte[] content = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(content);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception ex) {
            return Mono.error(ex);
        }
    }
}
