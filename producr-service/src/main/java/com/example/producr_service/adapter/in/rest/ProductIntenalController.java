package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.CartInfoRepone;
import com.example.producr_service.application.port.in.GetCartInfoUseCase;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProductIntenalController {
    GetCartInfoUseCase getCartInfoUseCase;

    @PostMapping("cart-info")
    public ResponseEntity<List<CartInfoRepone>> getCartInfo(
            @RequestBody @NotEmpty(message = "Danh sách variant không được để trống")
            List<@NotNull(message = "ID variant không được để trống") Long> variantIds) {
        return ResponseEntity.ok(getCartInfoUseCase.getCartInfo(variantIds));
    }
}
