package com.example.auth_service.adapter.in;

import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import com.example.auth_service.application.port.in.InternalGetUserInfoUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/internal")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class InternalUserController {
    private final InternalGetUserInfoUseCase internalGetUserInfoUseCase;
    @GetMapping("getInfoUser")
    UserInternaInfoRespone getInfoUser(@RequestHeader("Authorization") String authorizationHeader){
        return internalGetUserInfoUseCase.getInfoUserInternal(authorizationHeader);
    }

}
