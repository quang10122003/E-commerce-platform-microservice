package com.example.getway.application.port.out;

import com.example.getway.application.model.TokenIdentity;

public interface TokenVerificationPort {

    TokenIdentity verify(String token);
}
