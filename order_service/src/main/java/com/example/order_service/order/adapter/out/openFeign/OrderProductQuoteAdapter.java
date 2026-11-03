package com.example.order_service.order.adapter.out.openFeign;

import com.example.common.error.AuthorizationError;
import com.example.common.error.ErrorCode;
import com.example.common.exception.BusinessException;
import com.example.order_service.order.adapter.client.OrderProductFeignClient;
import com.example.order_service.order.application.dto.request.CheckoutQuoteRequest;
import com.example.order_service.order.application.dto.repone.CheckoutQuoteResponse;
import com.example.order_service.order.application.error.CheckoutQuoteError;
import com.example.order_service.order.application.port.out.client.ProductQuotePort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProductQuoteAdapter implements ProductQuotePort {
    private final OrderProductFeignClient productFeignClient;

    @Override
    public CheckoutQuoteResponse getProductQuote(CheckoutQuoteRequest request) {
        try {
            CheckoutQuoteResponse response = productFeignClient.getProductQuote(request);
            if (response == null || response.totalAmount() == null || response.totalAmount().signum() < 0) {
                throw new BusinessException(CheckoutQuoteError.PRICING_UNAVAILABLE);
            }
            return response;
        } catch (FeignException exception) {
            ErrorCode error = switch (exception.status()) {
                case 400 -> CheckoutQuoteError.INVALID_QUOTE;
                case 401 -> AuthorizationError.UNAUTHENTICATED;
                case 403 -> AuthorizationError.ACCESS_DENIED;
                case 404 -> CheckoutQuoteError.VARIANT_NOT_FOUND;
                case 409 -> CheckoutQuoteError.SALE_CONFLICT;
                default -> CheckoutQuoteError.PRICING_UNAVAILABLE;
            };
            throw new BusinessException(error, exception);
        }
    }
}
