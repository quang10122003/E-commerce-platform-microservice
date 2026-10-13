package com.example.producr_service.adapter.in.rest;

import com.example.producr_service.application.dto.response.UserFlashSaleResponse;
import com.example.producr_service.application.port.in.GetUserFlashSaleItemsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/user/flash-sales")
@RequiredArgsConstructor
public class UserFlashSaleController {
    private final GetUserFlashSaleItemsUseCase getUserFlashSaleItemsUseCase;

    // api lấy các thẻ sản phẩm đang sale cho user
    @GetMapping("/items")
    public ResponseEntity<UserFlashSaleResponse> getItems(
            @RequestParam(name = "categoryId", required = false) Long categoryId
    ) {
        return ResponseEntity.ok(getUserFlashSaleItemsUseCase.getItems(categoryId));
    }

    // Trả tối đa 5 card bán chạy còn AVAILABLE trong sale đang diễn ra.
    @GetMapping("/items/top-selling")
    public ResponseEntity<UserFlashSaleResponse> getTopSellingItems() {
        return ResponseEntity.ok(getUserFlashSaleItemsUseCase.getTopSellingItems());
    }
}
