package com.example.auth_service.application.port.out;

import com.example.auth_service.application.DTO.repone.AuthenticatedUser;

public interface AuthenticationManagerPort {

    public AuthenticatedUser authenticate(String email,String password);
    String encodePassword(String password);
   
}