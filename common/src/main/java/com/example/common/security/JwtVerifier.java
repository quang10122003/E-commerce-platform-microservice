package com.example.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

// Bean dùng chung để giải mã JWT bằng public key
@Component
public class JwtVerifier {
    private final PublicKey publicKey;

    // Nhận public key từ cấu hình để khởi tạo bộ kiểm tra token
    public JwtVerifier(@Value("${app.jwt.public-key}") String publicKeyBase64) {
        this.publicKey = RsaKeyUtils.loadPublicKey(publicKeyBase64);
    }

    // Giải mã token và trả về toàn bộ claims
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
