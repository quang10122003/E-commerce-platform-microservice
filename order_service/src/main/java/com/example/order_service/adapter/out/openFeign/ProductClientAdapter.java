package com.example.order_service.adapter.out.openFeign;

import com.example.order_service.adapter.client.ProductFeignClient;
import com.example.order_service.application.dto.response.ProductVariantInfoResponse;
import com.example.order_service.application.dto.request.ProductQuoteRequest;
import com.example.order_service.application.dto.response.ProductQuoteResponse;
import com.example.order_service.application.error.ProductQuoteError;
import com.example.common.error.ErrorCode;
import com.example.common.error.AuthorizationError;
import com.example.common.exception.BusinessException;
import feign.FeignException;
import com.example.order_service.application.port.out.client.ProductClientPort;
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
    public List<ProductVariantInfoResponse> getVariantInfo(List<Long> variantIds) {
        return productFeignClient.getVariantInfo(variantIds);
    }

    @Override
    public ProductQuoteResponse getProductQuote(ProductQuoteRequest request) {
        try {
            ProductQuoteResponse response = productFeignClient.getProductQuote(request);
            if (response == null || response.totalAmount() == null || response.totalAmount().signum() < 0) {
                throw new BusinessException(ProductQuoteError.PRICING_UNAVAILABLE);
            }
            return response;
        } catch (FeignException exception) {
            ErrorCode error = switch (exception.status()) {
                case 400 -> ProductQuoteError.INVALID_QUOTE;
                case 401 -> AuthorizationError.UNAUTHENTICATED;
                case 403 -> AuthorizationError.ACCESS_DENIED;
                case 404 -> ProductQuoteError.VARIANT_NOT_FOUND;
                case 409 -> ProductQuoteError.SALE_CONFLICT;
                default -> ProductQuoteError.PRICING_UNAVAILABLE;
            };
            throw new BusinessException(error, exception);
        }
    }

}
