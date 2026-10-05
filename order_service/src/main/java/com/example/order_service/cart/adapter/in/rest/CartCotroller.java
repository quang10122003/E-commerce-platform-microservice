package com.example.order_service.cart.adapter.in.rest;

import com.example.order_service.cart.application.dto.repone.CartRepone;
import com.example.order_service.cart.application.port.in.AddCartItemUseCase;
import com.example.order_service.cart.application.port.in.GetCartUseCase;
import com.example.order_service.cart.application.port.in.RemoveCartItemUseCase;
import com.example.order_service.cart.application.port.in.UpdateCartItemQuantityUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/cart")
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class CartCotroller {

    GetCartUseCase getCartUseCase;
    AddCartItemUseCase addCartItemUseCase;
    UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    RemoveCartItemUseCase removeCartItemUseCase;

    @GetMapping
    public ResponseEntity<CartRepone> getCart(){
        return ResponseEntity.ok(getCartUseCase.getCart());
    }
    @PostMapping()
    public ResponseEntity<Void> addCart(@RequestParam Long productVariantId){
        addCartItemUseCase.addCartItem(productVariantId);
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
}
