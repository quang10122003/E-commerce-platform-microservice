package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.BrandResponse;
import com.example.producr_service.application.port.in.GetBrandsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/brands")
public class BrandController {
    // Use case phục vụ thao tác lấy danh sách brand.
    private final GetBrandsUseCase getBrandsUseCase;

    // Trả về danh sách brand cho client.
    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        return ResponseEntity.ok(getBrandsUseCase.getBrands());
    }
}
