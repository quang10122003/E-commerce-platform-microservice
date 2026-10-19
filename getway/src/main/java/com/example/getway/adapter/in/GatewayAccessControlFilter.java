package com.example.getway.adapter.in;

import com.example.common.security.AuthorizationUtils;
import com.example.getway.application.port.in.AuthorizeAccessUseCase;

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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GatewayAccessControlFilter implements org.springframework.cloud.gateway.filter.GlobalFilter {

    AuthorizeAccessUseCase authorizeAccessUseCase;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authorizationHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null
                || authorizationHeader.isBlank()
                || authorizationHeader.trim().equalsIgnoreCase("Bearer")) {
            return chain.filter(exchange);
        }

        // Token đã gửi vẫn phải được kiểm tra, kể cả khi endpoint cho phép truy cập công khai.
        String token = AuthorizationUtils.extractBearerToken(authorizationHeader);
        authorizeAccessUseCase.authorize(token);
        return chain.filter(exchange);
    }
}
