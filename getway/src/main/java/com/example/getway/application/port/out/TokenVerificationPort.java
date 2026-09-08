package com.example.getway.application.port.out;

import com.example.getway.application.model.TokenIdentity;

// Định nghĩa cổng xác thực token cho application layer của Gateway.
public interface TokenVerificationPort {

    // Xác thực token và trả về userId đáng tin cậy.
    TokenIdentity verify(String token);
}
