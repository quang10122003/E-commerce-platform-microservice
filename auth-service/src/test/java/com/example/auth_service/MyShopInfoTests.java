package com.example.auth_service;

import com.example.auth_service.application.DTO.repone.PublicShopInfoResponse;
import com.example.auth_service.application.error.AuthError;
import com.example.auth_service.application.service.AuthServiceInternal;
import com.example.auth_service.application.port.out.TokenServicePort;
import com.example.auth_service.application.service.ShopProfileService;
import com.example.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Kiểm tra API hồ sơ shop của chính mình: shop id lấy từ token, dùng lại logic hồ sơ ở service internal.
class MyShopInfoTests {
    private final TokenServicePort tokens = mock(TokenServicePort.class);
    private final AuthServiceInternal publicShopInfo = mock(AuthServiceInternal.class);
    private final ShopProfileService service = new ShopProfileService(null, tokens, null, null, null, null, publicShopInfo);

    @Test
    void returnsShopOfUserInToken() {
        when(tokens.extractBearerToken("Bearer abc")).thenReturn("abc");
        when(tokens.getIdFromToken("abc")).thenReturn(9L);
        PublicShopInfoResponse expected = new PublicShopInfoResponse(9L, "Shop A", "Hà Nội", "https://cdn.test/logo.png",
                "Mô tả", Instant.parse("2026-01-01T00:00:00Z"));
        when(publicShopInfo.getPublicShopInfo(9L)).thenReturn(expected);

        assertEquals(expected, service.getMyShopInfo("Bearer abc"));
        verify(publicShopInfo).getPublicShopInfo(9L);
    }

    @Test
    void accountWithoutShopProfileIsNotFound() {
        when(tokens.extractBearerToken("Bearer abc")).thenReturn("abc");
        when(tokens.getIdFromToken("abc")).thenReturn(9L);
        when(publicShopInfo.getPublicShopInfo(9L)).thenThrow(new BusinessException(AuthError.SHOP_NOT_FOUND));

        BusinessException error = assertThrows(BusinessException.class, () -> service.getMyShopInfo("Bearer abc"));
        assertEquals(AuthError.SHOP_NOT_FOUND, error.getErrorCode());
    }
}
