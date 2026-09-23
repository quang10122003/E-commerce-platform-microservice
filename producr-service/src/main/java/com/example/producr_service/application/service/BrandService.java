package com.example.producr_service.application.service;

import com.example.producr_service.application.dto.response.BrandResponse;
import com.example.producr_service.application.port.in.GetBrandsUseCase;
import com.example.producr_service.application.port.out.repo.BrandRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class BrandService implements GetBrandsUseCase {
    // Cổng truy cập dữ liệu brand cho nghiệp vụ tra cứu.
    private final BrandRepositoryPort brandRepositoryPort;

    // Lấy brand từ repository và chuyển thành response ở application layer.
    @Transactional(readOnly = true)
    @Override
    public List<BrandResponse> getBrands() {
        return brandRepositoryPort.getAllBrands()
                .stream()
                .map(BrandResponse::from)
                .toList();
    }
}
