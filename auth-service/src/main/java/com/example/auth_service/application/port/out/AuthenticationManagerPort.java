package com.example.auth_service.application.port.out;

import com.example.auth_service.application.DTO.repone.AuthenticatedUser;

/**
 * AuthenticationManagerPort
 */
public interface AuthenticationManagerPort {
    public AuthenticatedUser authenticate(String email,String password);
   
}