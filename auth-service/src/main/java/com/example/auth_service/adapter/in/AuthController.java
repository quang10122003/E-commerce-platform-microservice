package com.example.auth_service.adapter.in;

import com.example.auth_service.application.DTO.repone.AccessTokenValidationResponse;
import com.example.auth_service.application.DTO.repone.AuthResponse;
import com.example.auth_service.application.DTO.repone.RefreshTokenRepone;
import com.example.auth_service.application.DTO.request.LoginRequest;
import com.example.auth_service.application.DTO.request.LogoutRequest;
import com.example.auth_service.application.DTO.request.RegisterRquest;
import com.example.auth_service.application.DTO.request.RefreshTokenRequest;
import com.example.auth_service.application.port.in.*;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {

   LoginUseCase loginUseCase;
   RegisterUseCase registerUseCase;
   RefreshTokenUseCase refreshTokenUseCase;
   ChecktokenUseCase checktokenUseCase;
   LogoutUserCase logoutUserCase;


    // API đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRquest request) {
        return ResponseEntity.ok(registerUseCase.register(request));
    }

    // API đăng nhập và trả token
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.login(request));
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<RefreshTokenRepone> refreshToken(@Valid @RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(refreshTokenUseCase.refreshToken(request));
    }

    @GetMapping("/check_token")
    public ResponseEntity<AccessTokenValidationResponse> accessTokenValidation(@RequestHeader("Authorization") String authorizationHeader){
        return ResponseEntity.ok(checktokenUseCase.CheckToken(authorizationHeader));
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(LogoutRequest request){
        return  ResponseEntity.noContent().build();
    }
}
