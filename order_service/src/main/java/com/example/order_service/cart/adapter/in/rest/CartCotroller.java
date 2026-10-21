package com.example.order_service.cart.adapter.in.rest;

import com.example.order_service.cart.application.dto.request.AddCartItemRequest;
import com.example.order_service.cart.application.dto.repone.CartRepone;
import com.example.order_service.cart.application.port.in.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class CartCotroller {

    GetCartUseCase getCartUseCase;
    AddCartItemUseCase addCartItemUseCase;
    UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    RemoveCartItemUseCase removeCartItemUseCase;
    GetCountItemCartUseCase getCountItemCartUseCase;

    @GetMapping
    public ResponseEntity<CartRepone> getCart(){
        return ResponseEntity.ok(getCartUseCase.getCart());
    }
    @PostMapping()
    public ResponseEntity<Void> addCart(@RequestParam Long productVariantId,
                                        @Valid @RequestBody(required = false) AddCartItemRequest request){
        addCartItemUseCase.addCartItem(productVariantId, request == null ? null : request.quantity());
        return ResponseEntity.status(201).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateCartItemQuantity(
            @RequestParam Long productVariantId,
            @RequestParam int quantity) {
        updateCartItemQuantityUseCase.updateCartItemQuantity(productVariantId, quantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> removeCartItem(@RequestParam Long productVariantId) {
        removeCartItemUseCase.removeCartItem(productVariantId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/count")
    public ResponseEntity<Integer> getCountItemCart(){
        return  ResponseEntity.status(200).body(getCountItemCartUseCase.getCountItemCart());
    }
}
