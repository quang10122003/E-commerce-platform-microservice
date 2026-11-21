package com.example.auth_service.application.service;

import com.example.auth_service.application.DTO.repone.AdminShopRequestResponse;
import com.example.auth_service.application.DTO.repone.ShopRequestResponse;
import com.example.auth_service.application.DTO.request.CreateShopRequestRequest;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.port.in.ApproveShopRequestUseCase;
import com.example.auth_service.application.port.in.CancelShopRequestUseCase;
import com.example.auth_service.application.port.in.CreateShopRequestUseCase;
import com.example.auth_service.application.port.in.GetMyShopRequestUseCase;
import com.example.auth_service.application.port.in.GetShopRequestsUseCase;
import com.example.auth_service.application.port.in.RejectShopRequestUseCase;
import com.example.auth_service.application.port.out.RoleRepositoryPort;
import com.example.auth_service.application.port.out.ShopRequestRepositoryPort;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.port.out.UserRepositoryPort;
import com.example.auth_service.domain.model.Role;
import com.example.auth_service.domain.model.RoleEnum;
import com.example.auth_service.domain.model.ShopRequest;
import com.example.auth_service.domain.model.ShopRequestStatus;
import com.example.auth_service.domain.model.User;
import com.example.common.exception.BusinessException;
import com.example.common.response.PageQuery;
import com.example.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ShopRequestService implements CreateShopRequestUseCase, GetMyShopRequestUseCase, CancelShopRequestUseCase,
        GetShopRequestsUseCase, ApproveShopRequestUseCase, RejectShopRequestUseCase {
    private final ShopRequestRepositoryPort shopRequestRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final TokenServicePort tokenServicePort;
    private final RoleRepositoryPort roleRepositoryPort;
    private final Clock clock;

    // Khóa dòng user để hai request nộp cùng lúc không vượt qua bước kiểm tra đơn đang chờ.
    @Override
    @Transactional
    public ShopRequestResponse createShopRequest(String authorizationHeader, CreateShopRequestRequest request) {
        Long userId = currentUserId(authorizationHeader);
        User user = userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        if (user.isShop()) {
            throw new BusinessException(AuthError.ALREADY_SHOP);
        }
        if (shopRequestRepositoryPort.existsPendingByUserId(userId)) {
            throw new BusinessException(AuthError.SHOP_REQUEST_PENDING_EXISTS);
        }
        ShopRequest saved = shopRequestRepositoryPort.save(ShopRequest.create(userId, request.shopName(),
                request.shopDescription(), request.shopPhone(), request.provinceCode(), request.provinceName(),
                request.ward(), request.addressDetail(), LocalDateTime.now(clock)));
        log.info("Nộp đơn mở shop: user_id={}, request_id={}", userId, saved.getId());
        return ShopRequestResponse.from(saved, clock.getZone());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShopRequestResponse> getMyLatestShopRequest(String authorizationHeader) {
        return shopRequestRepositoryPort.findLatestByUserId(currentUserId(authorizationHeader))
                .map((ShopRequest request) -> ShopRequestResponse.from(request, clock.getZone()));
    }

    // Lọc theo cả user id để không hủy được đơn của người khác.
    @Override
    @Transactional
    public ShopRequestResponse cancelShopRequest(String authorizationHeader, Long requestId) {
        Long userId = currentUserId(authorizationHeader);
        ShopRequest request = shopRequestRepositoryPort.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new BusinessException(AuthError.SHOP_REQUEST_NOT_FOUND));
        request.cancel(LocalDateTime.now(clock));
        ShopRequest saved = shopRequestRepositoryPort.save(request);
        log.info("Hủy đơn mở shop: user_id={}, request_id={}", userId, requestId);
        return ShopRequestResponse.from(saved, clock.getZone());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminShopRequestResponse> getShopRequests(ShopRequestStatus status, String keyword,
                                                                  PageQuery pageQuery) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return shopRequestRepositoryPort.findAdminPage(status, normalizedKeyword, pageQuery, clock.getZone());
    }

    // Khóa dòng user trước rồi mới khóa đơn, cùng thứ tự với lúc nộp đơn để hai luồng không chờ khóa vòng nhau.
    @Override
    @Transactional
    public AdminShopRequestResponse approveShopRequest(String authorizationHeader, Long requestId) {
        Long adminId = currentUserId(authorizationHeader);
        Long userId = shopRequestRepositoryPort.findById(requestId)
                .orElseThrow(() -> new BusinessException(AuthError.SHOP_REQUEST_NOT_FOUND)).getUserId();
        User user = userRepositoryPort.findByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthError.USER_NOT_FOUND));
        ShopRequest request = shopRequestRepositoryPort.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BusinessException(AuthError.SHOP_REQUEST_NOT_FOUND));
        Role shopRole = roleRepositoryPort.findByName(RoleEnum.ROLE_SHOP)
                .orElseThrow(() -> new BusinessException(AuthError.ROLE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now(clock);
        request.approve(adminId, now);
        user.becomeShop(shopRole, request.getShopName(), request.getShopDescription(), request.getShopPhone(),
                request.fullAddress(), request.getProvinceCode(), clock.instant());
        userRepositoryPort.save(user);
        shopRequestRepositoryPort.save(request);
        log.info("Duyệt đơn mở shop: request_id={}, user_id={}, admin_id={}", requestId, userId, adminId);
        return adminView(requestId);
    }

    @Override
    @Transactional
    public AdminShopRequestResponse rejectShopRequest(String authorizationHeader, Long requestId, String reason) {
        Long adminId = currentUserId(authorizationHeader);
        ShopRequest request = shopRequestRepositoryPort.findByIdForUpdate(requestId)
                .orElseThrow(() -> new BusinessException(AuthError.SHOP_REQUEST_NOT_FOUND));
        request.reject(adminId, reason, LocalDateTime.now(clock));
        shopRequestRepositoryPort.save(request);
        log.info("Từ chối đơn mở shop: request_id={}, user_id={}, admin_id={}", requestId, request.getUserId(), adminId);
        return adminView(requestId);
    }

    private AdminShopRequestResponse adminView(Long requestId) {
        return shopRequestRepositoryPort.findAdminView(requestId, clock.getZone())
                .orElseThrow(() -> new BusinessException(AuthError.SHOP_REQUEST_NOT_FOUND));
    }

    private Long currentUserId(String authorizationHeader) {
        return tokenServicePort.getIdFromToken(tokenServicePort.extractBearerToken(authorizationHeader));
    }
}
