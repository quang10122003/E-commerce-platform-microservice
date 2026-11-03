package com.example.producr_service.application.port.in;

import com.example.producr_service.application.dto.response.VariantInfoResponse;

import java.util.List;

public interface GetVariantInfoUseCase {
    List<VariantInfoResponse> getVariantInfo(List<Long> variantIds);
}
