package com.example.notification.adapter.in.rest;

import com.example.notification.application.dto.request.CreateBannerRequest;
import com.example.notification.application.dto.response.CreateBannerResponse;
import com.example.notification.application.port.in.CreateBannerUseCase;
import com.example.notification.application.port.in.UpdateBannerStatusUseCase;
import com.example.notification.application.dto.request.UpdateBannerStatusRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import com.example.notification.application.port.in.GetActiveBannersUseCase;
import com.example.notification.application.port.in.DeleteBannerUseCase;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.notification.application.port.in.GetAdminBannersUseCase;
import com.example.notification.application.dto.response.AdminBannerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final CreateBannerUseCase createBannerUseCase;
    private final GetActiveBannersUseCase getActiveBannersUseCase;
    private final GetAdminBannersUseCase getAdminBannersUseCase;
    private final DeleteBannerUseCase deleteBannerUseCase;
    private final UpdateBannerStatusUseCase updateBannerStatusUseCase;

    @PatchMapping("/{bannerId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> updateBannerStatus(@PathVariable("bannerId") @Positive Long bannerId,
                                                  @Valid @RequestBody UpdateBannerStatusRequest request) {
        updateBannerStatusUseCase.updateBannerStatus(bannerId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<String>> getActiveBannerHtml() {
        return ResponseEntity.ok(getActiveBannersUseCase.getActiveBannerHtml());
    }

    @DeleteMapping("/{bannerId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBanner(@PathVariable("bannerId") @Positive Long bannerId) {
        deleteBannerUseCase.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AdminBannerResponse>> getAllBanners() {
        return ResponseEntity.ok(getAdminBannersUseCase.getAllBanners());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CreateBannerResponse> createBanner(@Valid @RequestBody CreateBannerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createBannerUseCase.createBanner(request));
    }
}
