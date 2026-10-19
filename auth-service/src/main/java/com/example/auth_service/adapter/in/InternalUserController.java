package com.example.auth_service.adapter.in;

import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserDisplayInfoResponse;
import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import com.example.auth_service.application.port.in.GetPublicShopInfoUseCase;
import com.example.auth_service.application.port.in.InternalGetUserInfoUseCase;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/internal")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class InternalUserController {
    private final InternalGetUserInfoUseCase internalGetUserInfoUseCase;
    private final GetPublicShopInfoUseCase getPublicShopInfoUseCase;

    @PostMapping("user-info")
    List<UserDisplayInfoResponse> getUserDisplayInfosByIds(
            @RequestBody @Size(max = 50) List<@NotNull @Positive Long> userIds) {
        return internalGetUserInfoUseCase.getUserDisplayInfosByIds(userIds);
    }

    @GetMapping("/shops/{shopId}/info")
    public PublicShopInfoResponse getPublicShopInfo(@PathVariable("shopId") @Positive Long shopId) {
        return getPublicShopInfoUseCase.getPublicShopInfo(shopId);
    }
    @GetMapping("getInfoUser")
    UserInternaInfoRespone getInfoUser(@RequestHeader("Authorization") String authorizationHeader){
        return internalGetUserInfoUseCase.getInfoUserInternal(authorizationHeader);
    }

    @PostMapping("shop-info")
    List<ShopInfoResponse> getShopInfos(@RequestBody List<Long> userIds) {
        return internalGetUserInfoUseCase.getShopInfos(userIds);
    }

}
