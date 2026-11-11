package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.command.UploadFileCommand;
import com.example.auth_service.application.DTO.event.StoredFilesDeleteOutboxPayload;
import com.example.auth_service.application.DTO.repone.ShopLogoResponse;
import com.example.auth_service.application.constant.Constant;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import com.example.auth_service.application.port.in.GetMyShopInfoUseCase;
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

import static com.example.common.untill.FileUntils.getExtension;
import static com.example.common.untill.FileUntils.validateImage;

@Slf4j
@RequiredArgsConstructor
public class ShopProfileService implements UploadShopLogoUseCase, GetMyShopInfoUseCase {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShopLogoResponse uploadShopLogo(String authorizationHeader, UploadFileCommand image) {
        Long userId = tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
        // Khóa dòng để hai lần đổi logo đồng thời không cùng dọn một logo cũ và bỏ sót logo mới.
        User user = userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        user.assertCanEditShop();
        validateImage(image.contentType());
        if (image.content() == null || image.content().length == 0) {
            throw new BusinessException(AuthError.INVALID_IMAGE_FILE);
        }

        String oldObjectPath = user.getShopLogoObjectPath();
        String oldUrl = user.getShopLogoUrl();
        List<StoredFile> storedFiles = new ArrayList<>();
        // Đăng ký trước khi upload để logo mới được dọn nếu transaction rollback, kể cả lỗi lúc commit.
        storedFileRollbackPort.deleteImageStore(storedFiles);
        PreparedUpload upload = new PreparedUpload(StorageBucket.SHOP_LOGO,
                Constant.SHOP_LOGO_PREFIX + UUID.randomUUID() + getExtension(image.fileName()),
                image.contentType(), image.content());
        String publicUrl = fileStoragePort.upload(upload);
        storedFiles.add(new StoredFile(upload.bucket(), upload.objectPath(), publicUrl));

        user.changeShopLogo(publicUrl, upload.objectPath());
        userRepositoryPort.save(user);
        // Logo cũ chỉ bị xóa sau commit qua outbox; logo cũ chưa có object_path thì không dọn được.
        if (oldObjectPath != null) {
            saveDeleteFilesEvent(userId, new StoredFile(StorageBucket.SHOP_LOGO, oldObjectPath, oldUrl), outboxEventPort, clock);
        }
        log.info("Cập nhật logo shop thành công: shop_id={}", userId);
        return new ShopLogoResponse(publicUrl);
    }

    static void saveDeleteFilesEvent(Long userId, StoredFile file, OutboxEventPort outboxEventPort, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        outboxEventPort.save(new OutboxEvent(UUID.randomUUID(), Constant.USER, String.valueOf(userId),
                StoredFilesDeleteOutboxPayload.EVENT_TYPE, new StoredFilesDeleteOutboxPayload(List.of(file)), now));
    }
}
