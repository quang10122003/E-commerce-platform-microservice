package com.example.order_service.cart.adapter.out.openFeign;

import com.example.order_service.cart.adapter.client.ProductFeignClient;
import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import com.example.order_service.cart.application.dto.request.CartQuoteRequest;
import com.example.order_service.cart.application.dto.repone.CartQuoteResponse;
import com.example.order_service.cart.application.error.CartQuoteError;
import com.example.common.error.ErrorCode;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import feign.FeignException;
import com.example.order_service.cart.application.port.out.client.ProductClientPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductClientPort {
    ProductFeignClient productFeignClient;

    @Override
    public CartQuoteResponse getCartQuote(CartQuoteRequest request) {
        try {
            CartQuoteResponse response = productFeignClient.getCartQuote(request);
            if (response == null || response.totalAmount() == null || response.totalAmount().signum() < 0) {
                throw new BusinessException(CartQuoteError.PRICING_UNAVAILABLE);
            }
            return response;
        } catch (FeignException exception) {
            ErrorCode error = switch (exception.status()) {
                case 400 -> CartQuoteError.INVALID_QUOTE;
                case 401 -> AuthorizationError.UNAUTHENTICATED;
                case 403 -> AuthorizationError.ACCESS_DENIED;
                case 404 -> CartQuoteError.VARIANT_NOT_FOUND;
                case 409 -> CartQuoteError.SALE_CONFLICT;
                default -> CartQuoteError.PRICING_UNAVAILABLE;
            };
            throw new BusinessException(error, exception);
        }
    }

    @Override
    public List<CartInfoRepone> getCartInfo(List<Long> variantIds) {
        return productFeignClient.getCartInfo(variantIds);
    }
}
