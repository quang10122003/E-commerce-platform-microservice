package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.adapter.DTO.request.CreateBrandRequest;
import com.example.common.response.PageResponse;
import com.example.producr_service.application.dto.response.BrandResponse;
import com.example.producr_service.application.port.in.CreateBrandUseCase;
import com.example.producr_service.application.port.in.DeleteBrandUseCase;
import com.example.producr_service.application.port.in.GetAdminBrandsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/admin/brands")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminBrandController {
    private final GetAdminBrandsUseCase getAdminBrandsUseCase;
    private final DeleteBrandUseCase deleteBrandUseCase;
    private final CreateBrandUseCase createBrandUseCase;

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@RequestBody @Valid CreateBrandRequest request) {
        BrandResponse response = createBrandUseCase.createBrand(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> deleteBrand(@PathVariable @Positive Long brandId) {
        deleteBrandUseCase.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<BrandResponse>> getBrands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(getAdminBrandsUseCase.getAdminBrands(page, size, keyword));
    }
}
