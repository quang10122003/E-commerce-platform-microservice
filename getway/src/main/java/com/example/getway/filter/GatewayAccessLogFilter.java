package com.example.getway.filter;

import java.net.URI;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Gắn request_id và ghi log thông tin xử lý request tại gateway.
 */
@Component
public class GatewayAccessLogFilter implements org.springframework.cloud.gateway.filter.GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GatewayAccessLogFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = getOrCreateRequestId(exchange);
        long startTime = System.nanoTime();

        ServerWebExchange requestExchange = exchange.mutate()
                .request(request -> request.headers(headers -> headers.set(REQUEST_ID_HEADER, requestId)))
                .build();
        requestExchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        return chain.filter(requestExchange)
                .doFinally(signal -> logAccess(requestExchange, requestId, startTime));
    }

    private String getOrCreateRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER);
        return requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId;
    }

    private void logAccess(ServerWebExchange exchange, String requestId, long startTime) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        URI targetUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        HttpStatusCode status = exchange.getResponse().getStatusCode();
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;

        MDC.put("request_id", requestId);
        try {
            log.info(
                    "Gateway method={}, path={}, route={}, target={}, status={}, duration_ms={}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI().getRawPath(),
                    route == null ? "unknown" : route.getId(),
                    targetUri == null ? "unknown" : targetUri,
                    status == null ? "unknown" : status.value(),
                    durationMs);
        } finally {
            MDC.remove("request_id");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
