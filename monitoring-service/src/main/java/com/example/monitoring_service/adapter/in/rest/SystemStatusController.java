package com.example.monitoring_service.adapter.in.rest;

import com.example.monitoring_service.application.dto.response.SystemStatusResponse;
import com.example.monitoring_service.application.port.in.GetSystemStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system-status")
@RequiredArgsConstructor
public class SystemStatusController {

    private final GetSystemStatusUseCase getSystemStatusUseCase;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SystemStatusResponse> getSystemStatus() {
        return ResponseEntity.ok(getSystemStatusUseCase.getSystemStatus());
    }
}
