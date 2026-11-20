package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.command.UploadFileCommand;
import com.example.auth_service.application.DTO.event.StoredFilesDeleteOutboxPayload;
import com.example.auth_service.application.DTO.repone.AvatarResponse;
import com.example.auth_service.application.DTO.repone.ShopLogoResponse;
import com.example.auth_service.application.DTO.repone.UserProfileResponse;
import com.example.auth_service.application.DTO.request.UpdateProfileRequest;
import com.example.auth_service.application.DTO.request.UpdateShopProfileRequest;
import com.example.auth_service.application.constant.Constant;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import com.example.auth_service.application.port.in.GetMyProfileUseCase;
import com.example.auth_service.application.port.in.GetMyShopInfoUseCase;
import com.example.auth_service.application.port.in.UpdateMyProfileUseCase;
import com.example.auth_service.application.port.in.UpdateMyShopProfileUseCase;
import com.example.auth_service.application.port.in.UploadAvatarUseCase;
import com.example.auth_service.application.port.in.UploadShopLogoUseCase;
import com.example.auth_service.application.port.out.OutboxEventPort;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.application.port.out.storage.FileStoragePort;
import com.example.auth_service.application.port.out.storage.PreparedUpload;
import com.example.auth_service.application.port.out.storage.StorageBucket;
import com.example.auth_service.application.port.out.storage.StoredFile;
import com.example.auth_service.application.port.out.storage.StoredFileRollbackPort;
import com.example.auth_service.domain.model.OutboxEvent;
import com.example.auth_service.domain.model.Role;
import com.example.auth_service.domain.model.User;
import com.example.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.common.untill.FileUntils.getExtension;

@Slf4j
@RequiredArgsConstructor
public class ShopProfileService implements UploadShopLogoUseCase, UploadAvatarUseCase, GetMyShopInfoUseCase, GetMyProfileUseCase,
        UpdateMyProfileUseCase, UpdateMyShopProfileUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final TokenServicePort tokenServicePort;
    private final FileStoragePort fileStoragePort;
    private final StoredFileRollbackPort storedFileRollbackPort;
    private final OutboxEventPort outboxEventPort;
    private final Clock clock;
    private final AuthServiceInternal authServiceInternal;

    // Shop id lấy từ token (shop id chính là user id) để shop chỉ xem hồ sơ của mình; dùng lại logic hồ sơ ở service internal.
    @Override
    public PublicShopInfoResponse getMyShopInfo(String authorizationHeader) {
        Long shopId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        return authServiceInternal.getPublicShopInfo(shopId);
    }

    // User id lấy từ token nên chỉ xem được hồ sơ của chính mình.
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String authorizationHeader) {
        Long userId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        return toProfile(user);
    }

    // Không khóa dòng: hai lần sửa họ tên cùng lúc hiếm, lần ghi sau thắng là chấp nhận được.
    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(String authorizationHeader, UpdateProfileRequest request) {
        Long userId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.changeFullName(request.fullName());
        User saved = userRepositoryPort.save(user);
        log.info("Cập nhật thông tin cá nhân thành công: user_id={}", userId);
        return toProfile(saved);
    }

    // Đơn cũ giữ tên shop đã chụp lúc đặt; chỉ đơn mới và trang shop dùng tên mới.
    @Override
    @Transactional
    public PublicShopInfoResponse updateMyShopProfile(String authorizationHeader, UpdateShopProfileRequest request) {
        Long userId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.changeShopProfile(request.shopName(), request.shopDescription());
        userRepositoryPort.save(user);
        log.info("Cập nhật hồ sơ shop thành công: shop_id={}", userId);
        return authServiceInternal.getPublicShopInfo(userId);
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(user.getEmail(), user.getFullName(), user.getAvatarUrl(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt() == null ? null : user.getCreatedAt().atZone(clock.getZone()).toOffsetDateTime(),
                user.getProvider());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopLogoResponse uploadShopLogo(String authorizationHeader, UploadFileCommand image) {
        Long userId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        // Khóa dòng để hai lần đổi logo đồng thời không cùng dọn một logo cũ và bỏ sót logo mới.
        User user = userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.assertCanEditShop();
        String oldObjectPath = user.getShopLogoObjectPath();
        String oldUrl = user.getShopLogoUrl();
        StoredFile uploaded = uploadImage(StorageBucket.SHOP_LOGO, Constant.SHOP_LOGO_PREFIX, image);
        String publicUrl = uploaded.publicUrl();

        user.changeShopLogo(publicUrl, uploaded.objectPath());
        userRepositoryPort.save(user);
        // Logo cũ chỉ bị xóa sau commit qua outbox; logo cũ chưa có object_path thì không dọn được.
        if (oldObjectPath != null) {
            saveDeleteFilesEvent(userId, new StoredFile(StorageBucket.SHOP_LOGO, oldObjectPath, oldUrl), outboxEventPort, clock);
        }
        log.info("Cập nhật logo shop thành công: shop_id={}", userId);
        return new ShopLogoResponse(publicUrl);
    }

    // Không khóa dòng: hai lần đổi avatar cùng lúc rất hiếm, chấp nhận có thể sót một file avatar cũ trên storage.
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AvatarResponse uploadAvatar(String authorizationHeader, UploadFileCommand image) {
        Long userId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        String oldObjectPath = user.getAvatarObjectPath();
        String oldUrl = user.getAvatarUrl();
        StoredFile uploaded = uploadImage(StorageBucket.USER_AVATAR, Constant.USER_AVATAR_PREFIX, image);

        user.changeAvatar(uploaded.publicUrl(), uploaded.objectPath());
        userRepositoryPort.save(user);
        // Avatar cũ chỉ bị xóa sau commit qua outbox.
        if (oldObjectPath != null) {
            saveDeleteFilesEvent(userId, new StoredFile(StorageBucket.USER_AVATAR, oldObjectPath, oldUrl), outboxEventPort, clock);
        }
        log.info("Cập nhật avatar thành công: user_id={}", userId);
        return new AvatarResponse(uploaded.publicUrl());
    }

    // Kiểm tra ảnh rồi upload; đăng ký rollback trước khi upload để file mới được dọn nếu transaction rollback, kể cả lỗi lúc commit.
    private StoredFile uploadImage(StorageBucket bucket, String prefix, UploadFileCommand image) {
        // Tự kiểm tra loại file để trả INVALID_IMAGE_FILE (400) thay vì IllegalArgumentException của common bị trả 500.
        if (image.contentType() == null || !image.contentType().startsWith("image/")
                || image.content() == null || image.content().length == 0) {
            throw new BusinessException(AuthError.INVALID_IMAGE_FILE);
        }
        List<StoredFile> storedFiles = new ArrayList<>();
        storedFileRollbackPort.deleteImageStore(storedFiles);
        PreparedUpload upload = new PreparedUpload(bucket, prefix + UUID.randomUUID() + getExtension(image.fileName()),
                image.contentType(), image.content());
        StoredFile uploaded = new StoredFile(bucket, upload.objectPath(), fileStoragePort.upload(upload));
        storedFiles.add(uploaded);
        return uploaded;
    }

    static void saveDeleteFilesEvent(Long userId, StoredFile file, OutboxEventPort outboxEventPort, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        outboxEventPort.save(new OutboxEvent(UUID.randomUUID(), Constant.USER, String.valueOf(userId),
                StoredFilesDeleteOutboxPayload.EVENT_TYPE, new StoredFilesDeleteOutboxPayload(List.of(file)), now));
    }
}
