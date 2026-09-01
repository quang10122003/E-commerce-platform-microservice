package com.example.auth_service.infrastructure.sercurity;

import io.jsonwebtoken.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.security.PrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.auth_service.domain.model.Role;
import com.example.auth_service.domain.model.User;
import com.example.common.security.JwtVerifier;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthUtil {

    PrivateKey privateKey;
    long accessTokenExpirationMillis;
    long refreshTokenExpirationMillis;
    JwtVerifier jwtVerifier;

    public AuthUtil(
            @Value("${app.jwt.private-key}") String privateKeyBase64,
            @Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays,
            JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
        this.privateKey = RsaKeyUtil.loadPrivateKey(privateKeyBase64);
        this.accessTokenExpirationMillis = Duration.ofMinutes(accessTokenExpirationMinutes).toMillis();
        this.refreshTokenExpirationMillis = Duration.ofDays(refreshTokenExpirationDays).toMillis();
    }

    public String generateAccessToken(User user) {
        return generateToken(user, "access", accessTokenExpirationMillis);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, "refresh", refreshTokenExpirationMillis);
    }

    private String generateToken(User user, String tokenType, long expirationMillis) {
        Instant now = Instant.now();
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> name.startsWith("ROLE_") ? name : "ROLE_" + name)
                .collect(Collectors.toSet());

        return Jwts.builder()
                .subject(user.getEmail())
                .claims(Map.of(
                        "userId", user.getId(),
                        "roles", roleNames,
                        "fullName", user.getFullName(),
                        "isLocked", user.isLocked(),
                        "tokenType", tokenType))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "access");
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, "refresh");
    }

    // check token hớp lệ hay ko
    private boolean isTokenValid(String token, UserDetails userDetails, String expectedTokenType) {
        final String email = extractEmail(token);
        final String tokenType = extractTokenType(token);
        return email.equals(userDetails.getUsername())
                && expectedTokenType.equals(tokenType)
                && !isTokenExpired(token);
    }

    // check toke còn hạn k
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // giải mã token
    private Claims extractAllClaims(String token) {
        return jwtVerifier.extractAllClaims(token);
    }

}
