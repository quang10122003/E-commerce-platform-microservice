package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.CartInfoRepone;

import java.util.List;

public interface GetCartInfoUseCase {
    List<CartInfoRepone> getCartInfo(List<Long> variantIds);
}
