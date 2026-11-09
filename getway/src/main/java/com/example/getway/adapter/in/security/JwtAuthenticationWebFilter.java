package com.example.getway.adapter.in.security;

import com.example.common.security.JwtVerifier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

// Không đăng ký thành bean: WebFlux tự gắn mọi WebFilter bean vào toàn bộ request, filter này chỉ dùng trong chain actuator.
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtVerifier jwtVerifier;

    public JwtAuthenticationWebFilter(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return chain.filter(exchange);
        }

        Authentication authentication = toAuthentication(authorization.substring(BEARER_PREFIX.length()));
        if (authentication == null) {
            return chain.filter(exchange);
        }
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    private Authentication toAuthentication(String token) {
        try {
            Claims claims = jwtVerifier.extractAllClaims(token);
            if (!"access".equals(claims.get("tokenType", String.class))) {
                return null;
            }
            List<?> roles = claims.get("roles", List.class);
            List<GrantedAuthority> authorities = roles == null
                    ? List.of()
                    : roles.stream()
                            .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(String.valueOf(role)))
                            .toList();
            return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }
}
