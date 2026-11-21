package com.example.auth_service.adapter.in.rest;

import com.example.auth_service.application.DTO.repone.UserAddressResponse;
import com.example.auth_service.application.DTO.request.CreateAddressRequest;
import com.example.auth_service.application.port.in.CreateMyAddressUseCase;
import com.example.auth_service.application.port.in.DeleteMyAddressUseCase;
import com.example.auth_service.application.port.in.GetMyAddressesUseCase;
import com.example.auth_service.application.port.in.SetDefaultAddressUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
@Validated
public class UserAddressController {
    private final GetMyAddressesUseCase getMyAddressesUseCase;
    private final CreateMyAddressUseCase createMyAddressUseCase;
    private final DeleteMyAddressUseCase deleteMyAddressUseCase;
    private final SetDefaultAddressUseCase setDefaultAddressUseCase;

    @PutMapping("/{addressId}/default")
    public UserAddressResponse setDefaultAddress(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                 @PathVariable @Positive Long addressId) {
        return setDefaultAddressUseCase.setDefaultAddress(authorizationHeader, addressId);
    }

    @GetMapping
    public List<UserAddressResponse> getMyAddresses(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return getMyAddressesUseCase.getMyAddresses(authorizationHeader);
    }

    @PostMapping
    public ResponseEntity<UserAddressResponse> createMyAddress(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                               @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createMyAddressUseCase.createMyAddress(authorizationHeader, request));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteMyAddress(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                @PathVariable @Positive Long addressId) {
        deleteMyAddressUseCase.deleteMyAddress(authorizationHeader, addressId);
        return ResponseEntity.noContent().build();
    }
}
