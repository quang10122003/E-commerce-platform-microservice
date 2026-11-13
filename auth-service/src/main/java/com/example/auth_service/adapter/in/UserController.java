package com.example.auth_service.adapter.in;

import com.example.auth_service.application.port.in.GetMyProfileUseCase;
import com.example.auth_service.application.port.in.UpdateMyProfileUseCase;
import com.example.auth_service.application.port.in.UpdateMyShopProfileUseCase;
import com.example.auth_service.application.DTO.request.UpdateShopProfileRequest;
import com.example.auth_service.application.port.in.ChangePasswordUseCase;
import com.example.auth_service.application.DTO.request.ChangePasswordRequest;
import com.example.auth_service.application.DTO.request.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.auth_service.application.port.in.UploadAvatarUseCase;
import com.example.auth_service.application.DTO.repone.UserProfileResponse;
import com.example.auth_service.application.port.in.UploadShopLogoUseCase;
import com.example.auth_service.application.DTO.repone.AvatarResponse;
import com.example.auth_service.application.port.in.GetMyShopInfoUseCase;
import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import com.example.auth_service.application.DTO.command.UploadFileCommand;
import com.example.auth_service.application.DTO.repone.ShopLogoResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.example.auth_service.application.port.in.DeleteUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import com.example.auth_service.application.DTO.repone.UserListResponse;
import com.example.auth_service.application.DTO.repone.UserLockResponse;
import com.example.auth_service.application.port.in.ToggleUserLockUseCase;
import com.example.auth_service.application.port.in.ToggleShopLockUseCase;
import com.example.auth_service.application.DTO.repone.ShopStatusResponse;
import com.example.auth_service.application.port.in.GetUsersUseCase;
import com.example.auth_service.application.port.in.GetUserOverviewUseCase;
import com.example.auth_service.application.DTO.repone.UserOverviewResponse;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import com.example.auth_service.domain.model.RoleEnum;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final GetUsersUseCase getUsersUseCase;
    private final GetUserOverviewUseCase getUserOverviewUseCase;
    private final ToggleUserLockUseCase toggleUserLockUseCase;
    private final ToggleShopLockUseCase toggleShopLockUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final UploadShopLogoUseCase uploadShopLogoUseCase;
    private final UploadAvatarUseCase uploadAvatarUseCase;
    private final GetMyProfileUseCase getMyProfileUseCase;
    private final UpdateMyProfileUseCase updateMyProfileUseCase;
    private final UpdateMyShopProfileUseCase updateMyShopProfileUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final GetMyShopInfoUseCase getMyShopInfoUseCase;

    // Hồ sơ shop của tài khoản đang đăng nhập cho Kênh Người Bán.
    @GetMapping("/me/shop")
    @PreAuthorize("hasAuthority('ROLE_SHOP')")
    public PublicShopInfoResponse getMyShopInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return getMyShopInfoUseCase.getMyShopInfo(authorizationHeader);
    }

    // Hiện sửa tên và mô tả shop; logo dùng PUT /me/shop-logo.
    @PutMapping("/me/shop")
    @PreAuthorize("hasAuthority('ROLE_SHOP')")
    public PublicShopInfoResponse updateMyShopProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                      @Valid @RequestBody UpdateShopProfileRequest request) {
        return updateMyShopProfileUseCase.updateMyShopProfile(authorizationHeader, request);
    }

    @PutMapping(value = "/me/shop-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_SHOP')")
    public ShopLogoResponse uploadShopLogo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                           @RequestPart("image") MultipartFile image) throws IOException {
        return uploadShopLogoUseCase.uploadShopLogo(authorizationHeader,
                new UploadFileCommand(image.getOriginalFilename(), image.getContentType(), image.getBytes()));
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return getMyProfileUseCase.getMyProfile(authorizationHeader);
    }

    // Hiện chỉ sửa họ tên; avatar dùng PUT /me/avatar.
    @PutMapping("/me")
    public UserProfileResponse updateMyProfile(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                               @Valid @RequestBody UpdateProfileRequest request) {
        return updateMyProfileUseCase.updateMyProfile(authorizationHeader, request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordUseCase.changePassword(authorizationHeader, request);
        return ResponseEntity.noContent().build();
    }

    // Mọi tài khoản đăng nhập đều đổi được ảnh đại diện của chính mình.
    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AvatarResponse uploadAvatar(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                       @RequestPart("image") MultipartFile image) throws IOException {
        return uploadAvatarUseCase.uploadAvatar(authorizationHeader,
                new UploadFileCommand(image.getOriginalFilename(), image.getContentType(), image.getBytes()));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive Long userId) {
        deleteUserUseCase.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/shop/toggle-lock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ShopStatusResponse toggleShopLock(@PathVariable @Positive Long userId) {
        return toggleShopLockUseCase.toggleShopLock(userId);
    }

    @PostMapping("/{userId}/toggle-lock")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserLockResponse toggleUserLock(@PathVariable @Positive Long userId) {
        return toggleUserLockUseCase.toggleUserLock(userId);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserOverviewResponse getUserOverview() {
        return getUserOverviewUseCase.getUserOverview();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PageResponse<UserListResponse> getUsers(
            @RequestParam(required = false) RoleEnum role,
            @RequestParam(required = false) @Size(max = 255) String keyword,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(required = false) Boolean shopLocked,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return getUsersUseCase.getUsers(role, keyword, locked, shopLocked, new PageQuery(page, size));
    }
}
