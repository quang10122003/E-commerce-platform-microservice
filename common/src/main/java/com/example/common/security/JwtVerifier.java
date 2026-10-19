package com.example.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
public class JwtVerifier {
    private final PublicKey publicKey;

    public JwtVerifier(@Value("${app.jwt.public-key}") String publicKeyBase64) {
        this.publicKey = RsaKeyUtils.loadPublicKey(publicKeyBase64);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
