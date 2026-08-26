package com.example.auth_service.security;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthUtil {

    PrivateKey privateKey;
    PublicKey publicKey;
    long accessTokenExpirationMillis;
    long refreshTokenExpirationMillis;
    public AuthUtil(
            @Value("${app.jwt.private-key}") String privateKeyBase64,
            @Value("${app.jwt.public-key}") String publicKeyBase64,
            @Value("${app.jwt.access-token-expiration-minutes}") long accessTokenExpirationMinutes,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshTokenExpirationDays) {
        this.privateKey = RsaKeyUtil.loadPrivateKey(privateKeyBase64);
        this.publicKey = RsaKeyUtil.loadPublicKey(publicKeyBase64);
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
                        "role", roleNames,
                        "fullName", user.getFullName(),
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
        return Jwts.parser()
                .verifyWith(publicKey) // <-- verify bằng public key
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }



    // @Component
    // public class JwtVerifier {

    //     private final PublicKey publicKey;

    //     public JwtVerifier(@Value("${app.jwt.public-key}") String publicKeyBase64) {
    //         this.publicKey = RsaKeyUtil.loadPublicKey(publicKeyBase64);
    //     }

    //     public Claims extractAllClaims(String token) {
    //         return Jwts.parser()
    //                 .verifyWith(publicKey)
    //                 .build()
    //                 .parseSignedClaims(token)
    //                 .getPayload();
    //     }
    // }

//     @Component
// public class JwtAuthFilter extends OncePerRequestFilter {

//     private final JwtVerifier jwtVerifier;

//     public JwtAuthFilter(JwtVerifier jwtVerifier) {
//         this.jwtVerifier = jwtVerifier;
//     }

//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                      FilterChain filterChain) throws ServletException, IOException {

//         String authHeader = request.getHeader("Authorization");
//         if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//             filterChain.doFilter(request, response);
//             return;
//         }

//         String token = authHeader.substring(7);

//         try {
//             Claims claims = jwtVerifier.extractAllClaims(token);

//             String email = claims.getSubject();

//             // Sửa ở đây: đọc "roles" (List) thay vì "role" (String)
//             List<String> roles = claims.get("roles", List.class);

//             List<GrantedAuthority> authorities = roles == null
//                     ? List.of()
//                     : roles.stream()
//                         .map(SimpleGrantedAuthority::new) // đã có prefix ROLE_ sẵn từ AuthUtil
//                         .collect(Collectors.toList());

//             Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
//             SecurityContextHolder.getContext().setAuthentication(auth);

//         } catch (JwtException e) {
//             // token sai/hết hạn -> bỏ qua, request sẽ bị 401 nếu endpoint cần login
//         }

//         filterChain.doFilter(request, response);
//     }
// }
}