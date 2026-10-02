package com.example.order_service.cart.adapter.in.rest;

import com.example.order_service.cart.application.dto.repone.CartRepone;
import com.example.order_service.cart.application.port.in.GetCartUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("api/cart")
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CartCotroller {

    GetCartUseCase getCartUseCase;

    @GetMapping
    public ResponseEntity<CartRepone> getCart(){
        return ResponseEntity.ok(getCartUseCase.getCart());
    }
}
