package com.example.auth_service.adapter.in;

import com.example.auth_service.application.DTO.repone.ShopInfoResponse;
import com.example.auth_service.application.DTO.repone.UserInternaInfoRespone;
import com.example.auth_service.application.port.in.InternalGetUserInfoUseCase;
import com.example.auth_service.application.port.in.GetPublicShopInfoUseCase;
import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/internal")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class InternalUserController {
    private final InternalGetUserInfoUseCase internalGetUserInfoUseCase;
    private final GetPublicShopInfoUseCase getPublicShopInfoUseCase;

//    endpoint lấy thônng tin shop ko cần token
    @GetMapping("/shops/{shopId}/info")
    public PublicShopInfoResponse getPublicShopInfo(@PathVariable("shopId") @Positive Long shopId) {
        return getPublicShopInfoUseCase.getPublicShopInfo(shopId);
    }
    @GetMapping("getInfoUser")
    UserInternaInfoRespone getInfoUser(@RequestHeader("Authorization") String authorizationHeader){
        return internalGetUserInfoUseCase.getInfoUserInternal(authorizationHeader);
    }

    // Tra cứu tên shop theo lô để tránh gọi Auth service cho từng sản phẩm.
    @PostMapping("shop-info")
    List<ShopInfoResponse> getShopInfos(@RequestBody List<Long> userIds) {
        return internalGetUserInfoUseCase.getShopInfos(userIds);
    }

}
