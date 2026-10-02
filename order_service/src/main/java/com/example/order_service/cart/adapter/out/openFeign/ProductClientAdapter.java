package com.example.order_service.cart.adapter.out.openFeign;

import com.example.order_service.cart.adapter.client.ProductFeignClient;
import com.example.order_service.cart.application.dto.repone.CartInfoRepone;
import com.example.order_service.cart.application.dto.request.ProductCartInfoRequest;
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
    public List<CartInfoRepone> getCartInfo(List<Long> variantIds) {
        return productFeignClient.getCartInfo(new ProductCartInfoRequest(variantIds));
    }
}
