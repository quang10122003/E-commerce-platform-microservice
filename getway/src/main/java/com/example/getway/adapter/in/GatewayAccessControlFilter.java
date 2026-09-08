package com.example.getway.adapter.in;

import com.example.common.security.AuthorizationUtils;
import com.example.getway.application.service.AccessControlService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

// Adapter nhận request tại Gateway và kích hoạt use case kiểm soát truy cập.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GatewayAccessControlFilter implements org.springframework.cloud.gateway.filter.GlobalFilter {

    AccessControlService accessControlService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authorizationHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // API public vẫn cho phép request không có token đi tiếp.
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return chain.filter(exchange);
        }

        // Khi client gửi token, token phải hợp lệ và không bị thu hồi hoặc khóa user.
        String token = AuthorizationUtils.extractBearerToken(authorizationHeader);
        accessControlService.authorize(token);
        return chain.filter(exchange);
    }
}
